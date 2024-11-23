package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class CreateRaceRequest(
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: LocalDateTime? = null
)