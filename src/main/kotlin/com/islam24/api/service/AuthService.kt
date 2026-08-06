package com.islam24.api.service

import com.islam24.api.dto.auth.AuthRequest
import com.islam24.api.dto.auth.AuthResponse
import com.islam24.api.dto.auth.LogoutRequest
import com.islam24.api.dto.auth.RefreshRequest
import com.islam24.api.dto.auth.RefreshResponse
import com.islam24.api.error.exception.InvalidRefreshTokenException
import com.islam24.api.mapper.toEntity
import com.islam24.api.repository.RefreshTokenRepository
import com.islam24.api.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant


@Service
class AuthService(
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun googleLogin(authRequest: AuthRequest): AuthResponse {
        // Verify token with Google Web Token
        val googleUser = googleTokenVerifier.verify(token = authRequest.idToken)
        // Find if the user exist in database
        val user =
            userRepository.findByGoogleId(googleId = googleUser.googleId) ?: userRepository.save(googleUser.toEntity())

        val accessToken = jwtService.generateAccessToken(userId = user.id)
        val refreshToken = refreshTokenService.create(user= user)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken.token,
        )
    }

    @Transactional
    fun refresh(request: RefreshRequest): RefreshResponse {
        val refreshToken = refreshTokenRepository.findByToken(token = request.refreshToken) ?: throw InvalidRefreshTokenException()

        if (refreshToken.revoked){
            throw InvalidRefreshTokenException()
        }
        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            refreshToken.revoked = true
            throw InvalidRefreshTokenException()
        }

        val accessToken = jwtService.generateAccessToken(userId = refreshToken.user.id)
        return RefreshResponse(accessToken = accessToken)
    }

    @Transactional
    fun logout(request: LogoutRequest) {
        val refreshToken = refreshTokenRepository.findByToken(token = request.refreshToken) ?: throw InvalidRefreshTokenException()
        refreshToken.revoked = true
    }

}