package com.islam24.api.controller

import com.islam24.api.dto.profile.ProfileResponse
import com.islam24.api.dto.profile.UserSupportStatusResponse
import com.islam24.api.security.UserPrincipal
import com.islam24.api.service.ProfileService
import com.islam24.api.service.SupportService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profile")
class ProfileController(private val profileService: ProfileService, private val supportService: SupportService) {


    @GetMapping("/me")
    fun getProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ProfileResponse {
        return ProfileResponse(
            id = principal.id,
            googleId = principal.googleId,
            name = principal.name,
            email = principal.email,
            picture = principal.pictureUrl,
            createdAt = principal.createdAt,
        )
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ){
        profileService.deleteProfile(userId = principal.id)
    }

    @GetMapping("/support-status")
    fun getUserSupportStatus(
        @AuthenticationPrincipal principal: UserPrincipal
    ): UserSupportStatusResponse {
        return supportService.getUserSupportStatus(userId = principal.id)
    }

}