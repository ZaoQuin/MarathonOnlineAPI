package com.university.MarathonOnlineAPI.controller.auth

import com.university.MarathonOnlineAPI.entity.ERole

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val isVerified: Boolean
)
