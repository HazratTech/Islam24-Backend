package com.islam24.api.repository

import com.islam24.api.entity.UserTip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserTipRepository : JpaRepository<UserTip, UUID> {
}