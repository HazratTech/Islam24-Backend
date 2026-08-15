package com.islam24.api.mapper

import com.islam24.api.dto.sync.PrayerLogSyncDto
import com.islam24.api.dto.sync.PrayerSettingSyncDto
import com.islam24.api.entity.PrayerLogsEntity
import com.islam24.api.entity.User
import com.islam24.api.entity.UserPrayerSettingEntity
import java.util.UUID


fun PrayerLogsEntity.toDto() = PrayerLogSyncDto(
    logDate = this.logDate,
    fajr = this.fajr,
    dhuhr = this.dhuhr,
    asr = this.asr,
    maghrib = this.maghrib,
    isha = this.isha,
    updatedAt = this.updatedAt
)

fun PrayerLogSyncDto.toEntity(user: User) = PrayerLogsEntity(
    id = UUID.randomUUID(),
    user = user,
    logDate = this.logDate,
    fajr = this.fajr,
    dhuhr = this.dhuhr,
    asr = this.asr,
    maghrib = this.maghrib,
    isha = this.isha,
    updatedAt = this.updatedAt
)
