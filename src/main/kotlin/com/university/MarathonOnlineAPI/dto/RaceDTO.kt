package com.university.MarathonOnlineAPI.dto

import java.util.*

data class RaceDTO(
    val id: Long? = null,
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: Date? = null
)
