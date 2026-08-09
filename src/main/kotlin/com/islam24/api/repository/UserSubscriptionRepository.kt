package com.islam24.api.repository

import com.islam24.api.entity.User
import com.islam24.api.entity.UserSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Repository
interface UserSubscriptionRepository : JpaRepository<UserSubscription, UUID> {

    fun user(user: User): UserSubscription?

    fun findByUserId(userId: UUID): UserSubscription?

    @Query("""
        SELECT COUNT (DISTINCT u.id)
        FROM User u
        WHERE u.id IN (
        SELECT s.user.id FROM UserSubscription s
        WHERE s.status ='ACTIVE' AND (s.expiresAt IS NULL OR s.expiresAt > :now)
        ) OR u.id IN (
        SELECT t.user.id FROM UserTip t
        )
    """)
    fun countUniqueSupporters(@Param("now") now : Instant = Instant.now()) : Int


    @Query("SELECT COALESCE(SUM(s.price), 0.0) FROM UserSubscription s WHERE s.user.id = :userId")
    fun sumSubscriptionPriceInUsd(userId: UUID): Double
    @Query("SELECT COALESCE(SUM(s.priceInPurchasedCurrency), 0.0) FROM UserSubscription s WHERE s.user.id = :userId AND s.currency = :currency")
    fun sumSubscriptionPriceInLocalCurrency(userId: UUID, currency: String): Double

}