package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class RecentSurahSyncDto(
    @JsonProperty("surahNumber") @JsonAlias("surah_number", "surahNumber") val surahNumber: Int,
    @JsonProperty("surahName") @JsonAlias("surah_name", "surahName") val surahName: String,
    @JsonProperty("ayahNumber") @JsonAlias("ayah_number", "ayahNumber") val ayahNumber: Int,
    @JsonProperty("formattedDate") @JsonAlias("formatted_date", "formattedDate") val formattedDate: String,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)