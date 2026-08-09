package com.islam24.api.dto.profile

data class UserSupportStatusResponse(
    val isSupporter: Boolean,
    val totalContributionUsd: Double,   // Combined USD total
    val totalContributionLocal: Double, // Combined local currency total
    val localCurrency: String?,              // Code (e.g., "INR")
    val totalSupporter: Int
)
