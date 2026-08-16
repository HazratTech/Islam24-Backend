package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class KhatamPlanSyncDto(
    val id: UUID,
    val title: String = "Khatam Quran",
    @JsonProperty("startDateTimestamp") @JsonAlias("start_date_timestamp", "startDateTimestamp") val startDateTimestamp: Long,
    @JsonProperty("targetEndDateTimestamp") @JsonAlias("target_end_date_timestamp", "targetEndDateTimestamp") val targetEndDateTimestamp: Long,
    @JsonProperty("lastReadSurahNumber") @JsonAlias("last_read_surah_number", "lastReadSurahNumber") val lastReadSurahNumber: Int = 1,
    @JsonProperty("lastReadAyahNumber") @JsonAlias("last_read_ayah_number", "lastReadAyahNumber") val lastReadAyahNumber: Int = 1,
    @JsonProperty("lastReadGlobalAyahNumber") @JsonAlias("last_read_global_ayah_number", "lastReadGlobalAyahNumber") val lastReadGlobalAyahNumber: Int = 1,
    @JsonProperty("completedAyahsCount") @JsonAlias("completed_ayahs_count", "completedAyahsCount") val completedAyahsCount: Int = 0,
    val status: String = "IN_PROGRESS",
    @JsonProperty("completedTimestamp") @JsonAlias("completed_timestamp", "completedTimestamp") val completedTimestamp: Long? = null,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    @JsonProperty("updatedTimestamp") @JsonAlias("updated_timestamp", "updatedTimestamp") val updatedTimestamp: Long = System.currentTimeMillis()
)