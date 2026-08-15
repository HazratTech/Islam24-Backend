package com.islam24.api.service

import com.islam24.api.dto.sync.PrayerLogSyncDto
import com.islam24.api.dto.sync.PrayerSettingSyncDto
import com.islam24.api.dto.sync.SyncRequestDto
import com.islam24.api.dto.sync.SyncResponseDto
import com.islam24.api.entity.User
import com.islam24.api.entity.UserPrayerSettingEntity
import com.islam24.api.mapper.toDto
import com.islam24.api.mapper.toEntity
import com.islam24.api.repository.PrayerLogsRepository
import com.islam24.api.repository.UserPrayerSettingRepository
import com.islam24.api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncService(
    private val userRepository: UserRepository,
    private val userPrayerSettingRepository: UserPrayerSettingRepository,
    private val prayerLogsRepository: PrayerLogsRepository
) {

    private val logger = LoggerFactory.getLogger(SyncService::class.java)

    @Transactional
    fun syncUserData(userId: UUID, request: SyncRequestDto): SyncResponseDto {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("User not found: $userId") 
        }
        val serverSyncTimestamp = Instant.now()

        logger.info("SyncService: Sync received for user {}. lastSyncedAt={}, hasSettings={}, incomingLogsCount={}",
            userId, request.lastSyncedAt, request.prayerSettings != null, request.prayerLogs.size)

        // 1. Sync & Merge Prayer Settings (Last-Write-Wins based on updatedAt)
        val resolvedSettings = syncPrayerSettings(user = user, incoming = request.prayerSettings)

        // 2. Sync & Merge Prayer Logs (Day-by-Day Merge)
        val savedDates = syncPrayerLogs(user = user, incomingLogs = request.prayerLogs)

        // 3. Gather server updates to send back to client (Delta Sync)
        val updatedLogsForClient = if (request.lastSyncedAt == null) {
            // First sync or fresh login: return all historical records from server
            prayerLogsRepository.findByUserId(userId)
        } else {
            // Incremental sync: return only records modified after client's last sync
            prayerLogsRepository.findByUserIdAndUpdatedAtAfter(userId, request.lastSyncedAt)
        }

        return SyncResponseDto(
            syncedAt = serverSyncTimestamp,
            prayerSettings = resolvedSettings?.toDto(),
            prayerLogs = updatedLogsForClient.map { it.toDto() },
            syncedLogDates = savedDates
        )
    }

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

        // Server timestamp is newer or equal -> keep server version
        return existing
    }

    private fun syncPrayerLogs(user: User, incomingLogs: List<PrayerLogSyncDto>): List<java.time.LocalDate> {
        val savedDates = mutableListOf<java.time.LocalDate>()
        for (incoming in incomingLogs) {
            val existing = prayerLogsRepository.findByUserIdAndLogDate(user.id, incoming.logDate)

            if (existing == null) {
                logger.info("SyncService: Inserting new prayer log for user {} on date {}", user.id, incoming.logDate)
                prayerLogsRepository.save(incoming.toEntity(user = user))
                savedDates.add(incoming.logDate)
            } else {
                logger.info("SyncService: Merging existing prayer log for user {} on date {}", user.id, incoming.logDate)
                // Merge completed prayer flags (completed 'true' status is preserved)
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
}