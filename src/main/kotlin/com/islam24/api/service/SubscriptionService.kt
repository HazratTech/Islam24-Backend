package com.islam24.api.service

import com.islam24.api.dto.RevenueCatEvent
import com.islam24.api.entity.User
import com.islam24.api.entity.UserSubscription
import com.islam24.api.entity.UserTip
import com.islam24.api.entity.WebhookEventLog
import com.islam24.api.repository.UserRepository
import com.islam24.api.repository.UserSubscriptionRepository
import com.islam24.api.repository.UserTipRepository
import com.islam24.api.repository.WebhookEventLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val webhookEventLogRepository: WebhookEventLogRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userTipRepository: UserTipRepository,
) {

    @Transactional
    fun handleRevenueCatEvent(event: RevenueCatEvent) {
        // 1. Idempotency Check & Logging
        if (webhookEventLogRepository.existsById(event.id)) {
            return
        }
        webhookEventLogRepository.save(WebhookEventLog(eventId = event.id))

        // 2. Resolve User by UUID, email, or aliases
        val user = resolveUser(event) ?: return

        // 3. Process Webhook Event Type
        when (event.type) {
            "INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION", "TEST" -> {
                activeSubscription(user = user, event = event)
            }
            "NON_RENEWING_PURCHASE" -> {
                recordOneTimeTip(user = user, event = event)
            }
            "EXPIRATION" -> {
                expireSubscription(userId = user.id)
            }
            "CANCELLATION" -> {
                markAutoRenewDisabled(userId = user.id)
            }
        }
    }

    private fun resolveUser(event: RevenueCatEvent): User? {
        // Try finding by UUID if appUserId is a UUID string
        try {
            val uuid = UUID.fromString(event.appUserId)
            val user = userRepository.findById(uuid).orElse(null)
            if (user != null) return user
        } catch (_: IllegalArgumentException) {}

        // Try finding by email if appUserId contains @
        if (event.appUserId.contains("@")) {
            val user = userRepository.findByEmail(event.appUserId)
            if (user != null) return user
        }

        // Try finding by email/googleId from aliases list
        event.aliases?.forEach { alias ->
            if (alias.contains("@")) {
                val user = userRepository.findByEmail(alias)
                if (user != null) return user
            }
            val user = userRepository.findByGoogleId(alias)
            if (user != null) return user
        }

        return null
    }

    private fun activeSubscription(user: User, event: RevenueCatEvent) {
        val expiresAt = event.expirationAtMs?.let { Instant.ofEpochMilli(it) }
        val purchasedAt = event.purchasedAtMs?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        val entitlementId = event.entitlementIds?.firstOrNull() ?: event.entitlementId ?: "premium"
        val productId = event.productId ?: "subscription"
        val store = event.store ?: "UNKNOWN"
        val environment = event.environment ?: "PRODUCTION"

        val sub = userSubscriptionRepository.findByUserId(user.id)
            ?: UserSubscription(
                id = UUID.randomUUID(),
                user = user,
                productId = productId,
                entitlementId = entitlementId,
                status = "ACTIVE",
                store = store,
                environment = environment,
                purchasedAt = purchasedAt,
                expiresAt = expiresAt,
                updatedAt = Instant.now()
            )

        sub.status = "ACTIVE"
        sub.productId = productId
        sub.entitlementId = entitlementId
        sub.store = store
        sub.environment = environment
        sub.purchasedAt = purchasedAt
        sub.expiresAt = expiresAt
        sub.updatedAt = Instant.now()

        userSubscriptionRepository.save(sub)
    }

    private fun recordOneTimeTip(user: User, event: RevenueCatEvent) {
        val purchasedAt = event.purchasedAtMs?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        val tip = UserTip(
            id = UUID.randomUUID(),
            user = user,
            eventId = event.id,
            productId = event.productId ?: "one_time_tip",
            store = event.store ?: "STORE",
            purchasedAt = purchasedAt,
            createdAt = Instant.now()
        )
        userTipRepository.save(tip)
    }

    private fun expireSubscription(userId: UUID) {
        userSubscriptionRepository.findByUserId(userId)?.let { sub ->
            sub.status = "EXPIRED"
            sub.updatedAt = Instant.now()
            userSubscriptionRepository.save(sub)
        }
    }

    private fun markAutoRenewDisabled(userId: UUID) {
        userSubscriptionRepository.findByUserId(userId)?.let { sub ->
            sub.updatedAt = Instant.now()
            userSubscriptionRepository.save(sub)
        }
    }
}