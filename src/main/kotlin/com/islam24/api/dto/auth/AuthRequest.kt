package com.islam24.api.dto.auth

import jakarta.validation.constraints.NotBlank

data class AuthRequest(
    @field:NotBlank
    val idToken: String,
)
