package com.islam24.api.repository

import com.islam24.api.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID


@Repository
interface RefreshTokenRepository: JpaRepository<RefreshToken, UUID> {

    fun findByTokenHash(token: String): RefreshToken?

    fun deleteAllByUserId(userId: UUID)
}