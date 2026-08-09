package com.islam24.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "user_prayer_settings")
class UserPrayerSettingEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var calculationMethod : Int,

    @Column(nullable = false)
    var juristicMethod : Int,

    @Column(nullable = true)
    var masterNotification: Boolean = true,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var notificationSettings: NotificationSettings,

    @Column(nullable = false)
    var updatedAt : Instant = Instant.now()
)


data class NotificationSettings(
    val fajr : PrayerNotification,
    val dhuhr : PrayerNotification,
    val asr : PrayerNotification,
    val maghrib : PrayerNotification,
    val isha: PrayerNotification,
)

data class PrayerNotification(
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val audio: String = "default",
)