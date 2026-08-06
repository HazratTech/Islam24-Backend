package com.islam24.api.controller

import com.islam24.api.dto.auth.AuthRequest
import com.islam24.api.dto.auth.AuthResponse
import com.islam24.api.dto.auth.LogoutRequest
import com.islam24.api.dto.auth.RefreshRequest
import com.islam24.api.dto.auth.RefreshResponse
import com.islam24.api.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/google")
    fun googleAuth(
        @Valid @RequestBody request: AuthRequest
    ) : AuthResponse {
        return authService.googleLogin(authRequest = request)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshRequest
    ): RefreshResponse{
        return authService.refresh(request = request)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @Valid @RequestBody request: LogoutRequest
    ){
        authService.logout(request = request)
    }

}