package com.islam24.api.repository.prayer

import com.islam24.api.entity.prayer.UserPrayerSettingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserPrayerSettingRepository : JpaRepository<UserPrayerSettingEntity, UUID> {
    fun findByUserId(userId: UUID): UserPrayerSettingEntity?
}