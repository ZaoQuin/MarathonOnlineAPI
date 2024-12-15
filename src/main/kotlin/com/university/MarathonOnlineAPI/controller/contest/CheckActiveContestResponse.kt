package com.university.MarathonOnlineAPI.controller.contest

data class CheckActiveContestResponse(
    val exists: Boolean,
    val message: String? = null
)
