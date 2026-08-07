package com.islam24.api.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*


@Entity
@Table(name = "user_subscriptions")
class UserSubscription(

    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column
    var originalAppUserId: String? = null,

    @Column(nullable = false, length = 128)
    var productId: String,

    @Column(nullable = true,)
    var entitlementId: String? = null,

    @Column(nullable = false, length = 50)
    var status: String,

    @Column(nullable = false, length = 50)
    var store: String,

    @Column(nullable = false, length = 20)
    var environment: String,

    @Column(nullable = false)
    var purchasedAt: Instant,

    @Column
    var expiresAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
