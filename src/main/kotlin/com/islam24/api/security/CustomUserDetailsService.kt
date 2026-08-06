package com.islam24.api.security

import com.islam24.api.error.exception.UserNotFoundException
import com.islam24.api.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService{

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username) ?: throw UsernameNotFoundException(username)
        return UserPrincipal(user)
    }

    fun loadUserById(id: UUID): UserPrincipal {
        val user = userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }

        return UserPrincipal(user = user)
    }
}