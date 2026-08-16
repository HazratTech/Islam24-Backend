package com.islam24.api.repository.prayer

import com.islam24.api.entity.prayer.PrayerLogsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Repository
interface PrayerLogsRepository : JpaRepository<PrayerLogsEntity, UUID> {
    fun findByUserIdAndLogDate(userId: UUID, logDate: LocalDate): PrayerLogsEntity?
    fun findByUserIdAndUpdatedAtAfter(userId: UUID, updatedAt: Instant): List<PrayerLogsEntity>
    fun findByUserId(userId: UUID): List<PrayerLogsEntity>
}
