package com.islam24.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RevenueCatPayload(
    @JsonProperty("api_version") val apiVersion: String?,
    val event: RevenueCatEvent
)

data class RevenueCatEvent(
    val id: String,
    val type: String,
    @JsonProperty("app_user_id") val appUserId: String,
    val aliases: List<String>?,
    @JsonProperty("original_app_user_id") val originalAppUserId: String?,
    @JsonProperty("product_id") val productId: String?,
    @JsonProperty("entitlement_id") val entitlementId: String?,
    @JsonProperty("entitlement_ids") val entitlementIds: List<String>?,
    @JsonProperty("purchased_at_ms") val purchasedAtMs: Long?,
    @JsonProperty("expiration_at_ms") val expirationAtMs: Long?,
    val store: String?,
    val environment: String?,
)
