package com.university.MarathonOnlineAPI.controller.auth

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)
