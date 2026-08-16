package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.QuranBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuranBookmarkRepository : JpaRepository<QuranBookmarkEntity, UUID> {
    fun findByIdAndUserId(id: UUID, userId: UUID): QuranBookmarkEntity?
    fun findByUserId(userId: UUID): List<QuranBookmarkEntity>
    fun findByUserIdAndUpdatedAtAfter(userId: UUID, updatedAt: Long): List<QuranBookmarkEntity>
}