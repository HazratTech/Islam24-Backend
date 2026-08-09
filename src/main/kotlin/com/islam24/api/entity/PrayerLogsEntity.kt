package com.islam24.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "prayer_logs")
class PrayerLogsEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    var user: User,

    @Column(nullable = false)
    var logDate: LocalDate,

    @Column(nullable = false)
    var fajr: Boolean,
    @Column(nullable = false)
    var dhuhr: Boolean,
    @Column(nullable = false)
    var asr: Boolean,
    @Column(nullable = false)
    var maghrib: Boolean,
    @Column(nullable = false)
    var isha: Boolean,

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)
