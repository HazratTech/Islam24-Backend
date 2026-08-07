package com.islam24.api.service

import com.islam24.api.dto.RevenueCatEvent
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
    private val userTipRepository: UserTipRepository
) {

    @Transactional
    fun handleRevenueCatEvent(event: RevenueCatEvent) {
        // 1. Idempotency Check
        if (webhookEventLogRepository.existsById(event.id)) {
            return
        }

        val userId = try {
            UUID.fromString(event.appUserId)
        } catch (e: IllegalArgumentException) {
            return
        }

        when (event.type) {
            "INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION" -> {
                activeSubscription(userId = userId, event = event)
            }
            "NON_RENEWING_PURCHASE" -> {
                recordOneTimeTip(userId = userId, event = event)
            }
            "EXPIRATION" -> {
                expireSubscription(userId = userId)
            }
            "CANCELLATION" -> {
                markAutoRenewDisabled(userId = userId)
            }
        }

        // Log event ID to prevent duplicate processing
        webhookEventLogRepository.save(WebhookEventLog(eventId = event.id))
    }

    private fun activeSubscription(userId: UUID, event: RevenueCatEvent) {
        val user = userRepository.findById(userId).orElse(null) ?: return

        val expiresAt = event.expirationAtMs?.let { Instant.ofEpochMilli(it) }
        val purchasedAt = event.purchasedAtMs?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        val entitlementId = event.entitlementIds?.firstOrNull() ?: event.entitlementId ?: "premium"
        val productId = event.productId ?: "subscription"
        val store = event.store ?: "UNKNOWN"
        val environment = event.environment ?: "PRODUCTION"

        val sub = userSubscriptionRepository.findByUserId(userId)
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

    private fun recordOneTimeTip(userId: UUID, event: RevenueCatEvent) {
        val user = userRepository.findById(userId).orElse(null) ?: return

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