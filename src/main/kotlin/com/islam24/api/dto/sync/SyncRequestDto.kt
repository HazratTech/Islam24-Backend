package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant

data class SyncRequestDto(
    @JsonAlias("last_synced_at", "lastSyncedAt")
    val lastSyncedAt: Instant? = null,

    @JsonAlias("prayer_settings", "prayerSettings", "prayerSettingSyncDto")
    val prayerSettings: PrayerSettingSyncDto? = null,

    @JsonAlias("prayer_logs", "prayerLogs", "prayerLogSyncDto")
    val prayerLogs: List<PrayerLogSyncDto> = emptyList()
)
