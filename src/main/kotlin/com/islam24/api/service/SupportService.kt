package com.islam24.api.service

import com.islam24.api.dto.profile.UserSupportStatusResponse
import com.islam24.api.repository.UserSubscriptionRepository
import com.islam24.api.repository.UserTipRepository
import com.islam24.api.repository.UserPaymentRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


@Service
class SupportService(
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userTipRepository: UserTipRepository,
    private val userPaymentRepository: UserPaymentRepository
) {

    fun getUserSupportStatus(userId: UUID): UserSupportStatusResponse {
        val sub = userSubscriptionRepository.findByUserId(userId = userId)
        val expiresAt = sub?.expiresAt
        val hasActiveSub = sub != null && sub.status == "ACTIVE" &&
                (expiresAt == null || expiresAt.isAfter(Instant.now()))

        // 1. Calculate Combined USD Total from payment history
        val totalContributionUsd = userPaymentRepository.sumPriceInUsd(userId = userId)

        // 2. Resolve currency (Subscription first ➔ Payment history second ➔ Default INR)
        val userCurrency = sub?.currency 
            ?: userPaymentRepository.findLatestPaymentCurrency(userId = userId) 
            ?: "INR"

        // 3. Calculate Combined Local Currency Total from payment history
        val totalContributionLocal = userPaymentRepository.sumPriceInLocalCurrency(userId = userId, currency = userCurrency)

        // 4. Fetch global unique supporter count
        val totalSupporter = userPaymentRepository.countUniqueSupporters()

        val isSupporter = hasActiveSub || totalContributionUsd > 0

        return UserSupportStatusResponse(
            isSupporter = isSupporter,
            totalSupporter = totalSupporter,
            totalContributionUsd = totalContributionUsd,
            totalContributionLocal = totalContributionLocal,
            localCurrency = userCurrency
        )
    }

}