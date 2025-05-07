package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class RecordDTO(
    var id: Long? = null,
    var user: UserDTO? = null,
    var steps: Int? = null,
    var distance: Double? = null,
    var timeTaken: Long? = null,
    var avgSpeed: Double? = null,
    var timestamp: LocalDateTime? = null
)
