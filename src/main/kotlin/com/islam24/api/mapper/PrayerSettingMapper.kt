package com.islam24.api.mapper

import com.islam24.api.dto.sync.NotificationSettingDto
import com.islam24.api.dto.sync.NotificationSettingsMapDto
import com.islam24.api.dto.sync.PrayerSettingSyncDto
import com.islam24.api.entity.prayer.NotificationSettingEntity
import com.islam24.api.entity.prayer.NotificationSettingsMapEntity
import com.islam24.api.entity.User
import com.islam24.api.entity.prayer.UserPrayerSettingEntity
import java.util.UUID


fun UserPrayerSettingEntity.toDto(): PrayerSettingSyncDto {
    return PrayerSettingSyncDto(
        calculationMethod = calculationMethod,
        juristicMethod = juristicMethod,
        masterNotification = masterNotification,
        notificationSettings = notificationSettings.toDto(),
        updatedAt = updatedAt
    )
}


fun NotificationSettingsMapEntity.toDto(): NotificationSettingsMapDto {
    return NotificationSettingsMapDto(
        fajr = fajr.toDto(),
        dhuhr = dhuhr.toDto(),
        asr = asr.toDto(),
        maghrib = maghrib.toDto(),
        isha = isha.toDto()
    )
}

fun NotificationSettingEntity.toDto(): NotificationSettingDto {
    return NotificationSettingDto(
        enabled = enabled,
        offsetMinutes = offsetMinutes,
        audio = audio
    )
}



fun PrayerSettingSyncDto.toEntity(user: User) = UserPrayerSettingEntity(
    id = UUID.randomUUID(),
    user = user,
    calculationMethod = this.calculationMethod,
    juristicMethod = this.juristicMethod,
    masterNotification = this.masterNotification,
    notificationSettings = this.notificationSettings.toEntity(),
    updatedAt = this.updatedAt
)

fun NotificationSettingsMapDto.toEntity() : NotificationSettingsMapEntity {
    return NotificationSettingsMapEntity(
        fajr = fajr.toEntity(),
        dhuhr = dhuhr.toEntity(),
        asr = asr.toEntity(),
        maghrib = maghrib.toEntity(),
        isha = isha.toEntity(),
    )
}


fun NotificationSettingDto.toEntity(): NotificationSettingEntity {
    return NotificationSettingEntity(
        enabled = enabled,
        offsetMinutes = offsetMinutes,
        audio = audio,
    )
}