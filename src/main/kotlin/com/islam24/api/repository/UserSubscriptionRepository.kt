package com.islam24.api.repository

import com.islam24.api.entity.User
import com.islam24.api.entity.UserSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserSubscriptionRepository : JpaRepository<UserSubscription, UUID> {

    fun user(user: User): MutableList<UserSubscription>

    fun findByUserId(userId: UUID): UserSubscription?

}