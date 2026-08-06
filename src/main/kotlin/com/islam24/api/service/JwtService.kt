package com.islam24.api.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import javax.xml.crypto.Data


@Service
class JwtService {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(System.getenv("JWT_SECRET").toByteArray())

    fun generateAccessToken(userId: UUID): String {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(
                Date(
                    System.currentTimeMillis() + System.getenv("JWT_EXPIRATION").toLong()
                )
            )
            .signWith(secretKey)
            .compact()
    }

    fun extractToken(token: String): UUID {
        val claim = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)

        return UUID.fromString(claim.payload.subject)
    }

}