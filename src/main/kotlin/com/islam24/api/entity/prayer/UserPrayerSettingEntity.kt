package com.islam24.api.entity.prayer

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


import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.islam24.api.entity.User

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
    var notificationSettings: NotificationSettingsMapEntity = NotificationSettingsMapEntity(),

    @Column(nullable = false)
    var updatedAt : Instant = Instant.now()
)

data class NotificationSettingsMapEntity(
    @JsonProperty("fajr") val fajr: NotificationSettingEntity = NotificationSettingEntity(),
    @JsonProperty("dhuhr") val dhuhr: NotificationSettingEntity = NotificationSettingEntity(),
    @JsonProperty("asr") val asr: NotificationSettingEntity = NotificationSettingEntity(),
    @JsonProperty("maghrib") val maghrib: NotificationSettingEntity = NotificationSettingEntity(),
    @JsonProperty("isha") val isha: NotificationSettingEntity = NotificationSettingEntity()
)

data class NotificationSettingEntity(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("offsetMinutes") @JsonAlias("offset_minutes", "offsetMinutes") val offsetMinutes: Int = 0,
    @JsonProperty("audio") val audio: String = "default"
)