package com.islam24.api.controller

import com.islam24.api.dto.sync.SyncRequestDto
import com.islam24.api.dto.sync.SyncResponseDto
import com.islam24.api.security.UserPrincipal
import com.islam24.api.service.SyncService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sync")
class SyncController(
    private val syncService: SyncService
) {

    @PostMapping
    fun sync(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SyncRequestDto
    ): SyncResponseDto {
        return syncService.syncUserData(userId = principal.id, request = request)
    }
}
