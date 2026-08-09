package com.islam24.api.service

import com.islam24.api.dto.RevenueCatEvent
import com.islam24.api.dto.profile.LiveCommunityTickerPayload
import com.islam24.api.entity.User
import com.islam24.api.entity.UserPayment
import com.islam24.api.entity.UserSubscription
import com.islam24.api.entity.UserTip
import com.islam24.api.entity.WebhookEventLog
import com.islam24.api.repository.UserRepository
import com.islam24.api.repository.UserSubscriptionRepository
import com.islam24.api.repository.UserTipRepository
import com.islam24.api.repository.UserPaymentRepository
import com.islam24.api.repository.WebhookEventLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val webhookEventLogRepository: WebhookEventLogRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userTipRepository: UserTipRepository,
    private val userPaymentRepository: UserPaymentRepository,
    private val messagingTemplate: SimpMessagingTemplate
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
        } catch (_: IllegalArgumentException) {
        }

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
        sub.price = event.price?.toBigDecimal()
        sub.priceInPurchasedCurrency = event.priceInPurchasedCurrency?.toBigDecimal()
        sub.currency = event.currency
        sub.purchasedAt = purchasedAt
        sub.expiresAt = expiresAt
        sub.updatedAt = Instant.now()

        userSubscriptionRepository.save(sub)

        // Record subscription payment history
        val price = event.price?.toBigDecimal() ?: java.math.BigDecimal.ZERO
        val priceLocal = event.priceInPurchasedCurrency?.toBigDecimal() ?: price
        val currency = event.currency ?: "USD"
        userPaymentRepository.save(
            UserPayment(
                id = UUID.randomUUID(),
                user = user,
                eventId = event.id,
                type = "SUBSCRIPTION",
                price = price,
                priceInPurchasedCurrency = priceLocal,
                currency = currency,
                purchasedAt = purchasedAt
            )
        )

        if (event.type == "INITIAL_PURCHASE") {
            broadcastTicker(
                user = user,
                eventId = event.id,
                type = "SUBSCRIPTION",
                amount = event.priceInPurchasedCurrency ?: event.price ?: 0.0,
                currency = event.currency ?: "USD",
                productId = event.productId ?: "subscription",
            )
        }
    }

    private fun recordOneTimeTip(user: User, event: RevenueCatEvent) {
        val purchasedAt = event.purchasedAtMs?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        val tip = UserTip(
            id = UUID.randomUUID(),
            user = user,
            eventId = event.id,
            productId = event.productId ?: "one_time_tip",
            store = event.store ?: "STORE",
            price = event.price?.toBigDecimal(),
            priceInPurchasedCurrency = event.priceInPurchasedCurrency?.toBigDecimal(),
            currency = event.currency,
            purchasedAt = purchasedAt,
            createdAt = Instant.now()
        )
        userTipRepository.save(tip)

        // Record tip payment history
        val tipPrice = event.price?.toBigDecimal() ?: java.math.BigDecimal.ZERO
        val tipPriceLocal = event.priceInPurchasedCurrency?.toBigDecimal() ?: tipPrice
        val tipCurrency = event.currency ?: "USD"
        userPaymentRepository.save(
            UserPayment(
                id = UUID.randomUUID(),
                user = user,
                eventId = event.id,
                type = "TIP",
                price = tipPrice,
                priceInPurchasedCurrency = tipPriceLocal,
                currency = tipCurrency,
                purchasedAt = purchasedAt
            )
        )

        broadcastTicker(
            user = user,
            eventId = event.id,
            type = "TIP",
            amount = event.priceInPurchasedCurrency ?: event.price ?: 0.0,
            currency = event.currency ?: "USD",
            productId = event.productId ?: "one_time_tip",
        )
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


    private fun broadcastTicker(
        eventId: String,
        user: User,
        type: String,
        amount: Double,
        currency: String,
        productId: String,
    ) {
        val payload = LiveCommunityTickerPayload(
            eventId = eventId,
            donorName = user.displayName,
            avatarUrl = user.avatarUrl.toString(),
            type = type,
            amount = amount,
            currency = currency,
            productId = productId,
        )

        messagingTemplate.convertAndSend("/topic/community-updates", payload)
    }

}