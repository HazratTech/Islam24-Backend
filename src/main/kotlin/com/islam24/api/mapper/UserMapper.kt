package com.islam24.api.mapper

import com.islam24.api.dto.profile.ProfileResponse
import com.islam24.api.dto.auth.GoogleUser
import com.islam24.api.entity.User
import java.time.Instant


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
        createdAt = createdAt ?: Instant.now()
    )
}