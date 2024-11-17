package com.university.MarathonOnlineAPI.controller.user

data class UpdatePasswordRequest(
    val email: String,
    val password: String
)
