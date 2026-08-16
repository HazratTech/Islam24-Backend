package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant

data class SyncRequestDto(
    @JsonAlias("last_synced_at", "lastSyncedAt")
    val lastSyncedAt: Instant? = null,

    // Prayer Sync
    @JsonAlias("prayer_settings", "prayerSettings", "prayerSettingSyncDto")
    val prayerSettings: PrayerSettingSyncDto? = null,
    @JsonAlias("prayer_logs", "prayerLogs", "prayerLogSyncDto")
    val prayerLogs: List<PrayerLogSyncDto> = emptyList(),

    // Quran Sync
    @JsonAlias("khatam_plans", "khatamPlans", "khatamPlanSyncDto")
    val khatamPlans: List<KhatamPlanSyncDto> = emptyList(),
    @JsonAlias("quran_bookmarks", "quranBookmarks", "quranBookmarkSyncDto")
    val quranBookmarks: List<QuranBookmarkSyncDto> = emptyList(),
    @JsonAlias("recent_surahs", "recentSurahs", "recentSurahSyncDto")
    val recentSurahs: List<RecentSurahSyncDto> = emptyList()
)
