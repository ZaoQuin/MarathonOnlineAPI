package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class CreateRecordRequest(
    val steps: Int? = null,
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: LocalDateTime? = null
)