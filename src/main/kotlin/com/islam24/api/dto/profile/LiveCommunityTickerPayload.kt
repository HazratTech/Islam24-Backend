package com.islam24.api.dto.profile

import java.time.Instant

data class LiveCommunityTickerPayload(
    val eventId: String,
    val donorName: String,
    val avatarUrl: String,
    val type: String, // "TIP" or "SUBSCRIPTION"
    val amount: Double?,
    val currency: String?,
    val productId: String?,
    val createdAt: Instant = Instant.now(),
)
