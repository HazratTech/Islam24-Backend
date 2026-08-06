package com.islam24.api.dto.auth

data class GoogleUser(
    val googleId: String,
    val name: String,
    val email: String,
    val pictureUrl: String,
)
