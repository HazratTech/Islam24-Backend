package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SyncResponseDto(
    @JsonAlias("synced_at", "syncedAt")
    val syncedAt: Instant,

    // Prayer Response
    @JsonAlias("prayer_settings", "prayerSettings")
    val prayerSettings: PrayerSettingSyncDto? = null,
    @JsonAlias("prayer_logs", "prayerLogs")
    val prayerLogs: List<PrayerLogSyncDto> = emptyList(),
    @JsonAlias("synced_log_dates", "syncedLogDates")
    val syncedLogDates: List<LocalDate> = emptyList(),

    // Quran Remote Updates (for Android to merge)
    val khatamPlans: List<KhatamPlanSyncDto> = emptyList(),
    val quranBookmarks: List<QuranBookmarkSyncDto> = emptyList(),
    val recentSurahs: List<RecentSurahSyncDto> = emptyList(),

    // Explicit Deletion ACKs (for Android to permanently DELETE from Room)
    val confirmedDeletedKhatamIds: List<UUID> = emptyList(),
    val confirmedDeletedBookmarkIds: List<UUID> = emptyList(),
    val confirmedDeletedRecentSurahNumbers: List<Int> = emptyList()
)
