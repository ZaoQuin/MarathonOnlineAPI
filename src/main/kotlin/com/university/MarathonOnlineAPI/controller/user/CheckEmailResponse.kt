package com.university.MarathonOnlineAPI.controller.user

data class CheckEmailResponse(
    val exists: Boolean,
    val message: String? = null
)
