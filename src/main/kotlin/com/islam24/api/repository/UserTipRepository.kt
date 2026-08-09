package com.islam24.api.repository

import com.islam24.api.entity.UserTip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserTipRepository : JpaRepository<UserTip, UUID> {

    fun countByUserId(userId: UUID): Int

    @Query("SELECT COALESCE(SUM(t.price), 0.0) FROM UserTip t WHERE t.user.id = :userId")
    fun sumTipPriceInUsd(userId: UUID): Double
    @Query("SELECT COALESCE(SUM(t.priceInPurchasedCurrency), 0.0) FROM UserTip t WHERE t.user.id = :userId AND t.currency = :currency")
    fun sumTipPriceInLocalCurrency(userId: UUID, currency: String): Double
    @Query("SELECT t.currency FROM UserTip t WHERE t.user.id = :userId ORDER BY t.purchasedAt DESC LIMIT 1")
    fun findLatestTipCurrency(userId: UUID): String?
}