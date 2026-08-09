package com.islam24.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_payments")
class UserPayment(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "event_id", unique = true, nullable = false)
    var eventId: String,

    @Column(nullable = false)
    var type: String, // 'TIP' or 'SUBSCRIPTION'

    @Column(precision = 10, scale = 2, nullable = false)
    var price: BigDecimal,

    @Column(name = "price_in_purchased_currency", precision = 10, scale = 2, nullable = false)
    var priceInPurchasedCurrency: BigDecimal,

    @Column(nullable = false)
    var currency: String,

    @Column(name = "purchased_at", nullable = false)
    var purchasedAt: Instant,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
