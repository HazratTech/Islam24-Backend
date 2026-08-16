package com.islam24.api.service

import com.islam24.api.dto.sync.*
import com.islam24.api.entity.User
import com.islam24.api.entity.prayer.UserPrayerSettingEntity
import com.islam24.api.mapper.toDto
import com.islam24.api.mapper.toEntity
import com.islam24.api.repository.UserRepository
import com.islam24.api.repository.prayer.PrayerLogsRepository
import com.islam24.api.repository.prayer.UserPrayerSettingRepository
import com.islam24.api.repository.quran.KhatamPlanRepository
import com.islam24.api.repository.quran.QuranBookmarkRepository
import com.islam24.api.repository.quran.RecentSurahRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.math.max

@Service
class SyncService(
    private val userRepository: UserRepository,
    private val userPrayerSettingRepository: UserPrayerSettingRepository,
    private val prayerLogsRepository: PrayerLogsRepository,
    private val khatamPlanRepository: KhatamPlanRepository,
    private val quranBookmarkRepository: QuranBookmarkRepository,
    private val recentSurahRepository: RecentSurahRepository
) {

    private val logger = LoggerFactory.getLogger(SyncService::class.java)

    @Transactional
    fun syncUserData(userId: UUID, request: SyncRequestDto): SyncResponseDto {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("User not found: $userId") 
        }
        val serverSyncTimestamp = Instant.now()

        logger.info(
            "SyncService: Sync received for user {}. lastSyncedAt={}, hasSettings={}, logsCount={}, khatamsCount={}, bookmarksCount={}, recentCount={}",
            userId, request.lastSyncedAt, request.prayerSettings != null,
            request.prayerLogs.size, request.khatamPlans.size, request.quranBookmarks.size, request.recentSurahs.size
        )

        // 1. Sync & Merge Prayer Settings (Last-Write-Wins based on updatedAt)
        val resolvedSettings = syncPrayerSettings(user = user, incoming = request.prayerSettings)

        // 2. Sync & Merge Prayer Logs (Day-by-Day Merge)
        val savedPrayerDates = syncPrayerLogs(user = user, incomingLogs = request.prayerLogs)

        // 3. Sync & Merge Quran Khatam Plans (Max Global Ayah Wins for in-progress, soft delete handling)
        val confirmedDeletedKhatams = syncKhatamPlans(user = user, incomingList = request.khatamPlans)

        // 4. Sync & Merge Quran Bookmarks (LWW with Tombstone)
        val confirmedDeletedBookmarks = syncBookmarks(user = user, incomingList = request.quranBookmarks)

        // 5. Sync & Merge Recent Surahs (Max Ayah Wins per Surah)
        val confirmedDeletedRecent = syncRecentSurahs(user = user, incomingList = request.recentSurahs)

        // 6. Gather server updates to send back to client (Delta Sync)
        val updatedLogsForClient = if (request.lastSyncedAt == null) {
            prayerLogsRepository.findByUserId(userId)
        } else {
            prayerLogsRepository.findByUserIdAndUpdatedAtAfter(userId, request.lastSyncedAt)
        }

        val updatedKhatamsForClient = if (request.lastSyncedAt == null) {
            khatamPlanRepository.findByUserId(userId)
        } else {
            khatamPlanRepository.findByUserIdAndUpdatedTimestampAfter(userId, request.lastSyncedAt.toEpochMilli())
        }

        val updatedBookmarksForClient = if (request.lastSyncedAt == null) {
            quranBookmarkRepository.findByUserId(userId)
        } else {
            quranBookmarkRepository.findByUserIdAndUpdatedAtAfter(userId, request.lastSyncedAt.toEpochMilli())
        }

        val updatedRecentForClient = if (request.lastSyncedAt == null) {
            recentSurahRepository.findByUserId(userId)
        } else {
            recentSurahRepository.findByUserIdAndTimestampAfter(userId, request.lastSyncedAt.toEpochMilli())
        }

        return SyncResponseDto(
            syncedAt = serverSyncTimestamp,
            prayerSettings = resolvedSettings?.toDto(),
            prayerLogs = updatedLogsForClient.map { it.toDto() },
            syncedLogDates = savedPrayerDates,
            khatamPlans = updatedKhatamsForClient.filter { !it.isDeleted }.map { it.toDto() },
            quranBookmarks = updatedBookmarksForClient.filter { !it.isDeleted }.map { it.toDto() },
            recentSurahs = updatedRecentForClient.filter { !it.isDeleted }.map { it.toDto() },
            confirmedDeletedKhatamIds = confirmedDeletedKhatams,
            confirmedDeletedBookmarkIds = confirmedDeletedBookmarks,
            confirmedDeletedRecentSurahNumbers = confirmedDeletedRecent
        )
    }

    // =========================================================================
    // 1. PRAYER SETTINGS MERGE
    // =========================================================================
    private fun syncPrayerSettings(
        user: User,
        incoming: PrayerSettingSyncDto?
    ): UserPrayerSettingEntity? {
        val existing = userPrayerSettingRepository.findByUserId(user.id)

        if (incoming == null) {
            return existing
        }

        if (existing == null) {
            return userPrayerSettingRepository.save(incoming.toEntity(user = user))
        }

        // Conflict Resolution: Client timestamp is newer -> update server record
        if (incoming.updatedAt.isAfter(existing.updatedAt)) {
            existing.calculationMethod = incoming.calculationMethod
            existing.juristicMethod = incoming.juristicMethod
            existing.masterNotification = incoming.masterNotification
            existing.notificationSettings = incoming.notificationSettings.toEntity()
            existing.updatedAt = incoming.updatedAt
            return userPrayerSettingRepository.save(existing)
        }

        return existing
    }

    // =========================================================================
    // 2. PRAYER LOGS MERGE
    // =========================================================================
    private fun syncPrayerLogs(user: User, incomingLogs: List<PrayerLogSyncDto>): List<LocalDate> {
        val savedDates = mutableListOf<LocalDate>()
        for (incoming in incomingLogs) {
            val existing = prayerLogsRepository.findByUserIdAndLogDate(user.id, incoming.logDate)

            if (existing == null) {
                logger.info("SyncService: Inserting new prayer log for user {} on date {}", user.id, incoming.logDate)
                prayerLogsRepository.save(incoming.toEntity(user = user))
                savedDates.add(incoming.logDate)
            } else {
                logger.info("SyncService: Merging existing prayer log for user {} on date {}", user.id, incoming.logDate)
                existing.fajr = existing.fajr || incoming.fajr
                existing.dhuhr = existing.dhuhr || incoming.dhuhr
                existing.asr = existing.asr || incoming.asr
                existing.maghrib = existing.maghrib || incoming.maghrib
                existing.isha = existing.isha || incoming.isha

                if (incoming.updatedAt.isAfter(existing.updatedAt)) {
                    existing.updatedAt = incoming.updatedAt
                }

                prayerLogsRepository.save(existing)
                savedDates.add(incoming.logDate)
            }
        }
        return savedDates
    }

    // =========================================================================
    // 3. KHATAM PLANS MERGE (Max Global Ayah Wins for IN_PROGRESS)
    // =========================================================================
    private fun syncKhatamPlans(user: User, incomingList: List<KhatamPlanSyncDto>): List<UUID> {
        val confirmedDeleted = mutableListOf<UUID>()
        for (incoming in incomingList) {
            val existing = khatamPlanRepository.findByIdAndUserId(id = incoming.id, userId = user.id)

            if (existing == null) {
                if (incoming.isDeleted) {
                    confirmedDeleted.add(incoming.id)
                } else {
                    khatamPlanRepository.save(incoming.toEntity(user = user))
                }
            } else {
                if (incoming.isDeleted) {
                    existing.isDeleted = true
                    existing.updatedTimestamp = max(existing.updatedTimestamp, incoming.updatedTimestamp)
                    khatamPlanRepository.save(existing)
                    confirmedDeleted.add(incoming.id)
                } else {
                    // Rule: Max Global Ayah Wins for In-Progress
                    if (incoming.lastReadGlobalAyahNumber > existing.lastReadGlobalAyahNumber) {
                        existing.lastReadGlobalAyahNumber = incoming.lastReadGlobalAyahNumber
                        existing.lastReadSurahNumber = incoming.lastReadSurahNumber
                        existing.lastReadAyahNumber = incoming.lastReadAyahNumber
                        existing.completedAyahsCount = incoming.completedAyahsCount
                    }
                    if (incoming.status == "COMPLETED") {
                        existing.status = "COMPLETED"
                        existing.completedTimestamp = incoming.completedTimestamp
                    }
                    existing.updatedTimestamp = max(existing.updatedTimestamp, incoming.updatedTimestamp)
                    existing.isDeleted = false
                    khatamPlanRepository.save(existing)
                }
            }
        }
        return confirmedDeleted
    }

    // =========================================================================
    // 4. QURAN BOOKMARKS MERGE (LWW + Tombstone)
    // =========================================================================
    private fun syncBookmarks(user: User, incomingList: List<QuranBookmarkSyncDto>): List<UUID> {
        val confirmedDeleted = mutableListOf<UUID>()
        for (incoming in incomingList) {
            val existing = quranBookmarkRepository.findByIdAndUserId(id = incoming.id, userId = user.id)

            if (existing == null) {
                if (incoming.isDeleted) {
                    confirmedDeleted.add(incoming.id)
                } else {
                    quranBookmarkRepository.save(incoming.toEntity(user = user))
                }
            } else {
                if (incoming.isDeleted) {
                    existing.isDeleted = true
                    existing.updatedAt = max(existing.updatedAt, incoming.updatedAt)
                    quranBookmarkRepository.save(existing)
                    confirmedDeleted.add(incoming.id)
                } else if (incoming.updatedAt > existing.updatedAt) {
                    existing.surahNumber = incoming.surahNumber
                    existing.ayahNumber = incoming.ayahNumber
                    existing.globalAyahNumber = incoming.globalAyahNumber
                    existing.updatedAt = incoming.updatedAt
                    existing.isDeleted = false
                    quranBookmarkRepository.save(existing)
                }
            }
        }
        return confirmedDeleted
    }

    // =========================================================================
    // 5. RECENT SURAHS MERGE (Max Ayah Number Wins)
    // =========================================================================
    private fun syncRecentSurahs(user: User, incomingList: List<RecentSurahSyncDto>): List<Int> {
        val confirmedDeleted = mutableListOf<Int>()
        for (incoming in incomingList) {
            val existing = recentSurahRepository.findByUserIdAndSurahNumber(userId = user.id, surahNumber = incoming.surahNumber)

            if (existing == null) {
                if (incoming.isDeleted) {
                    confirmedDeleted.add(incoming.surahNumber)
                } else {
                    recentSurahRepository.save(incoming.toEntity(user = user))
                }
            } else {
                if (incoming.isDeleted && incoming.timestamp >= existing.timestamp) {
                    existing.isDeleted = true
                    existing.timestamp = incoming.timestamp
                    recentSurahRepository.save(existing)
                    confirmedDeleted.add(incoming.surahNumber)
                } else {
                    // Rule: Max Ayah Number Wins
                    if (incoming.ayahNumber > existing.ayahNumber) {
                        existing.ayahNumber = incoming.ayahNumber
                        existing.formattedDate = incoming.formattedDate
                    }
                    existing.timestamp = max(existing.timestamp, incoming.timestamp)
                    existing.isDeleted = false
                    recentSurahRepository.save(existing)
                }
            }
        }
        return confirmedDeleted
    }
}