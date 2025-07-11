package com.university.MarathonOnlineAPI.controller.auth

import com.university.MarathonOnlineAPI.entity.ERole
import com.university.MarathonOnlineAPI.entity.EUserStatus

data class AuthenticationResponse(
    val fullName: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val status: EUserStatus
)
