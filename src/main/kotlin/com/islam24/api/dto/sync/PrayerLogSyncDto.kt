package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant
import java.time.LocalDate

data class PrayerLogSyncDto(
    @JsonAlias("log_date", "logDate")
    val logDate: LocalDate,

    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,

    @JsonAlias("updated_at", "updatedAt")
    val updatedAt: Instant
)
