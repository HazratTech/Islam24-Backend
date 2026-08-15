package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class PrayerSettingSyncDto(
    @JsonProperty("calculationMethod")
    @JsonAlias("calculation_method", "calculationMethod")
    val calculationMethod: Int = 1,

    @JsonProperty("juristicMethod")
    @JsonAlias("juristic_method", "juristicMethod")
    val juristicMethod: Int = 0,

    @JsonProperty("masterNotification")
    @JsonAlias("master_notification", "masterNotification")
    val masterNotification: Boolean = true,

    @JsonProperty("notificationSettings")
    @JsonAlias("notification_settings", "notificationSettings", "notificationSettingsJson")
    val notificationSettings: NotificationSettingsMapDto = NotificationSettingsMapDto(),

    @JsonProperty("updatedAt")
    @JsonAlias("updated_at", "updatedAt")
    val updatedAt: Instant = Instant.now()
)

data class NotificationSettingsMapDto(
    @JsonProperty("fajr") val fajr: NotificationSettingDto = NotificationSettingDto(),
    @JsonProperty("dhuhr") val dhuhr: NotificationSettingDto = NotificationSettingDto(),
    @JsonProperty("asr") val asr: NotificationSettingDto = NotificationSettingDto(),
    @JsonProperty("maghrib") val maghrib: NotificationSettingDto = NotificationSettingDto(),
    @JsonProperty("isha") val isha: NotificationSettingDto = NotificationSettingDto()
)

data class NotificationSettingDto(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("offsetMinutes") @JsonAlias("offset_minutes", "offsetMinutes") val offsetMinutes: Int = 0,
    @JsonProperty("audio") val audio: String = "default"
)