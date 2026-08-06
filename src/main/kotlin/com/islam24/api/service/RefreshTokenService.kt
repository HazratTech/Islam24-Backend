package com.islam24.api.service

import com.islam24.api.entity.RefreshToken
import com.islam24.api.entity.User
import com.islam24.api.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RefreshTokenService(private val refreshTokenRepository: RefreshTokenRepository) {

    fun create(user: User): RefreshToken {
        val token = UUID.randomUUID().toString()

        val refreshToken = RefreshToken(
            token = token,
            user = user,
            expiresAt = Instant.now().plusMillis(
                System.getenv("REFRESH_EXPIRE_MILI")?.toLong()
                    ?: throw IllegalStateException("REFRESH_EXPIRE_MILI must be set")
            ),
        )

        return refreshTokenRepository.save(refreshToken)
    }

}