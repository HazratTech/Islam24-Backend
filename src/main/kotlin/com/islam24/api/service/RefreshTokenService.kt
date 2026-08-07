package com.islam24.api.service

import com.islam24.api.entity.RefreshToken
import com.islam24.api.entity.User
import com.islam24.api.repository.RefreshTokenRepository
import com.islam24.api.security.hashToken
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RefreshTokenService(private val refreshTokenRepository: RefreshTokenRepository) {

    fun create(user: User): String {
        val rawToken = UUID.randomUUID().toString()
        val hashToken = hashToken(token = rawToken)

        val refreshToken = RefreshToken(
            tokenHash = hashToken,
            user = user,
            expiresAt = Instant.now().plusMillis(
                System.getenv("REFRESH_EXPIRE_MILI")?.toLong()
                    ?: throw IllegalStateException("REFRESH_EXPIRE_MILI must be set")
            ),
        )
        refreshTokenRepository.save(refreshToken)
        return rawToken
    }

}