package com.university.MarathonOnlineAPI.controller.contest

data class CheckContestNameResponse(
    val exists: Boolean,
    val message: String? = null
)
