package com.islam24.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "users")
class User(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(unique = true, nullable = false)
    var googleId: String,
    @Column(nullable = false)
    var displayName: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column
    var avatarUrl: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant? = Instant.now(),
    @Column(name = "updated_at")
    var updatedAt: Instant? = Instant.now(),
)
