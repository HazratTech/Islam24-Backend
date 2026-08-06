package com.islam24.api.controller

import com.islam24.api.dto.ProfileResponse
import com.islam24.api.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profile")
class ProfileController {


    @GetMapping("/me")
    fun getProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ProfileResponse {
        return ProfileResponse(
            id = principal.id,
            googleId = principal.googleId,
            name = principal.name,
            email = principal.email,
            picture = principal.pictureUrl
        )
    }

}