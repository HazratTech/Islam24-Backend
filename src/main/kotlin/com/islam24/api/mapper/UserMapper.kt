package com.islam24.api.mapper

import com.islam24.api.dto.ProfileResponse
import com.islam24.api.dto.auth.GoogleUser
import com.islam24.api.entity.User


fun GoogleUser.toEntity(): User {
    return User(
        googleId =  googleId,
        displayName = name,
        email = email,
        avatarUrl = pictureUrl,
    )
}

fun User.toResponse(): ProfileResponse {

    return ProfileResponse(
        id = id,
        googleId = googleId,
        name = displayName,
        email = email,
        picture = avatarUrl,
        createdAt = createdAt
    )
}