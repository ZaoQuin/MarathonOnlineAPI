package com.university.MarathonOnlineAPI.controller.user

data class CheckUsernameResponse (
    val exists: Boolean,
    val message: String? = null
)
