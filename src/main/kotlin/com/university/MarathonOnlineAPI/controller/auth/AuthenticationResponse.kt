package com.university.MarathonOnlineAPI.controller.auth

import com.university.MarathonOnlineAPI.entity.ERole

data class AuthenticationResponse(
    val fullName: String,
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val isVerified: Boolean,
    val isDeleted: Boolean
)
