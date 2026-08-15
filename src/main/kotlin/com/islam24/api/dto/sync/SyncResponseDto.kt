package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant
import java.time.LocalDate

data class SyncResponseDto(
    @JsonAlias("synced_at", "syncedAt")
    val syncedAt: Instant,

    @JsonAlias("prayer_settings", "prayerSettings")
    val prayerSettings: PrayerSettingSyncDto? = null,

    @JsonAlias("prayer_logs", "prayerLogs")
    val prayerLogs: List<PrayerLogSyncDto> = emptyList(),

    @JsonAlias("synced_log_dates", "syncedLogDates")
    val syncedLogDates: List<LocalDate> = emptyList()
)
