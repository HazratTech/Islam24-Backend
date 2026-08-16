package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class QuranBookmarkSyncDto(
    val id: UUID,
    @JsonProperty("surahNumber") @JsonAlias("surah_number", "surahNumber") val surahNumber: Int,
    @JsonProperty("ayahNumber") @JsonAlias("ayah_number", "ayahNumber") val ayahNumber: Int,
    @JsonProperty("globalAyahNumber") @JsonAlias("global_ayah_number", "globalAyahNumber") val globalAyahNumber: Int,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    @JsonProperty("updatedAt") @JsonAlias("updated_at", "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)