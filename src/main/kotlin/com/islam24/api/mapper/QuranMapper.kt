package com.islam24.api.mapper

import com.islam24.api.dto.sync.KhatamPlanSyncDto
import com.islam24.api.dto.sync.QuranBookmarkSyncDto
import com.islam24.api.dto.sync.RecentSurahSyncDto
import com.islam24.api.entity.User
import com.islam24.api.entity.quran.KhatamPlanEntity
import com.islam24.api.entity.quran.QuranBookmarkEntity
import com.islam24.api.entity.quran.RecentSurahEntity
import java.util.UUID

// 1. Khatam Plan Mappers
fun KhatamPlanEntity.toDto() = KhatamPlanSyncDto(
    id = this.id,
    title = this.title,
    startDateTimestamp = this.startDateTimestamp,
    targetEndDateTimestamp = this.targetEndDateTimestamp,
    lastReadSurahNumber = this.lastReadSurahNumber,
    lastReadAyahNumber = this.lastReadAyahNumber,
    lastReadGlobalAyahNumber = this.lastReadGlobalAyahNumber,
    completedAyahsCount = this.completedAyahsCount,
    status = this.status,
    completedTimestamp = this.completedTimestamp,
    isDeleted = this.isDeleted,
    updatedTimestamp = this.updatedTimestamp
)

fun KhatamPlanSyncDto.toEntity(user: User) = KhatamPlanEntity(
    id = this.id,
    user = user,
    title = this.title,
    startDateTimestamp = this.startDateTimestamp,
    targetEndDateTimestamp = this.targetEndDateTimestamp,
    lastReadSurahNumber = this.lastReadSurahNumber,
    lastReadAyahNumber = this.lastReadAyahNumber,
    lastReadGlobalAyahNumber = this.lastReadGlobalAyahNumber,
    completedAyahsCount = this.completedAyahsCount,
    status = this.status,
    completedTimestamp = this.completedTimestamp,
    isDeleted = this.isDeleted,
    updatedTimestamp = this.updatedTimestamp
)

// 2. Quran Bookmark Mappers
fun QuranBookmarkEntity.toDto() = QuranBookmarkSyncDto(
    id = this.id,
    surahNumber = this.surahNumber,
    ayahNumber = this.ayahNumber,
    globalAyahNumber = this.globalAyahNumber,
    isDeleted = this.isDeleted,
    updatedAt = this.updatedAt
)

fun QuranBookmarkSyncDto.toEntity(user: User) = QuranBookmarkEntity(
    id = this.id,
    user = user,
    surahNumber = this.surahNumber,
    ayahNumber = this.ayahNumber,
    globalAyahNumber = this.globalAyahNumber,
    isDeleted = this.isDeleted,
    updatedAt = this.updatedAt
)

// 3. Recent Surah Mappers
fun RecentSurahEntity.toDto() = RecentSurahSyncDto(
    surahNumber = this.surahNumber,
    surahName = this.surahName,
    ayahNumber = this.ayahNumber,
    formattedDate = this.formattedDate,
    isDeleted = this.isDeleted,
    timestamp = this.timestamp
)

fun RecentSurahSyncDto.toEntity(user: User) = RecentSurahEntity(
    id = UUID.randomUUID(),
    user = user,
    surahNumber = this.surahNumber,
    surahName = this.surahName,
    ayahNumber = this.ayahNumber,
    formattedDate = this.formattedDate,
    isDeleted = this.isDeleted,
    timestamp = this.timestamp
)