package com.islam24.api.dto

import java.util.UUID

data class ProfileResponse(
    val id: UUID,
    val googleId: String?,
    val name: String,
    val email: String,
    val picture: String?,
)
