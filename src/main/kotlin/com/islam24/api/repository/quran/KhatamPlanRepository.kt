package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.KhatamPlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KhatamPlanRepository : JpaRepository<KhatamPlanEntity, UUID> {
    fun findByIdAndUserId(id: UUID, userId: UUID): KhatamPlanEntity?
    fun findByUserId(userId: UUID): List<KhatamPlanEntity>
    fun findByUserIdAndUpdatedTimestampAfter(userId: UUID, timestamp: Long): List<KhatamPlanEntity>
}