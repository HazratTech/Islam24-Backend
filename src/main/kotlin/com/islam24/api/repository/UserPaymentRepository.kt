package com.islam24.api.repository

import com.islam24.api.entity.UserPayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserPaymentRepository : JpaRepository<UserPayment, UUID> {

    @Query("SELECT COALESCE(SUM(p.price), 0.0) FROM UserPayment p WHERE p.user.id = :userId")
    fun sumPriceInUsd(userId: UUID): Double

    @Query("SELECT COALESCE(SUM(p.priceInPurchasedCurrency), 0.0) FROM UserPayment p WHERE p.user.id = :userId AND p.currency = :currency")
    fun sumPriceInLocalCurrency(userId: UUID, currency: String): Double

    @Query("SELECT p.currency FROM UserPayment p WHERE p.user.id = :userId ORDER BY p.purchasedAt DESC LIMIT 1")
    fun findLatestPaymentCurrency(userId: UUID): String?

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM UserPayment p")
    fun countUniqueSupporters(): Int
}
