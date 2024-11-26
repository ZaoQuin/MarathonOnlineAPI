package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class RaceDTO(
    val id: Long? = null,
    var user: UserDTO? = null,
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: LocalDateTime? = null
)
