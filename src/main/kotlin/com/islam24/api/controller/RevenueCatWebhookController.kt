package com.islam24.api.controller

import com.islam24.api.dto.RevenueCatPayload
import com.islam24.api.service.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/webhooks")
class RevenueCatWebhookController(private val subscriptionService: SubscriptionService) {

    @PostMapping("/revenuecat")
    fun handleWebhook(
        @RequestBody payload: RevenueCatPayload
    ) : ResponseEntity<String> {
        return try {
            subscriptionService.handleRevenueCatEvent(event = payload.event)
            ResponseEntity.ok().build()
        }catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

}