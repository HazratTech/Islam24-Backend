package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.RecentSurahEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RecentSurahRepository : JpaRepository<RecentSurahEntity, UUID> {
    fun findByUserIdAndSurahNumber(userId: UUID, surahNumber: Int): RecentSurahEntity?
    fun findByUserId(userId: UUID): List<RecentSurahEntity>
    fun findByUserIdAndTimestampAfter(userId: UUID, timestamp: Long): List<RecentSurahEntity>
}