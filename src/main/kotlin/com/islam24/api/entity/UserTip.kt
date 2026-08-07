package com.islam24.api.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*


@Entity
@Table(name = "user_tips")
class UserTip(

    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 255)
    var eventId: String,

    @Column(nullable = false, length = 128)
    var productId: String,

    @Column(nullable = false, length = 50)
    var store: String,

    @Column(nullable = false)
    var purchasedAt: Instant,

    @Column(nullable = false)
    var createdAt: Instant,

    )