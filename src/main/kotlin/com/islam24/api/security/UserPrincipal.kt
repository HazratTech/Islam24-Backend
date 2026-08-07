package com.islam24.api.security

import com.islam24.api.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.util.UUID

class UserPrincipal(
    private val user: User
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

    override fun getPassword(): String? = null

    override fun getUsername(): String = user.email

    val id: UUID
        get() = user.id
    val name: String
        get() = user.displayName
    val email: String
        get() = user.email
    val pictureUrl: String?
        get() = user.avatarUrl

    val googleId: String
        get() = user.googleId

    val createdAt: Instant
        get() = user.createdAt ?: Instant.now()
}