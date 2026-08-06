package com.islam24.api.security

import com.islam24.api.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ")
            try {
                val userId = jwtService.extractToken(token = token)
                val userPrincipal = customUserDetailsService.loadUserById(id = userId)

                val authentication = UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                logger.warn("JWT Authentication failed: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}