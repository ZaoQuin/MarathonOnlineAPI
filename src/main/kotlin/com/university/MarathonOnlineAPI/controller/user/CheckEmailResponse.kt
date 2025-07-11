package com.university.MarathonOnlineAPI.controller.user

data class CheckEmailResponse(
    var exists: Boolean,
    var message: String? = null
)
