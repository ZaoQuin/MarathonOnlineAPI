package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class FeedbackDTO (
    var id: Long? = null,
    var sender: UserDTO? = null,
    var message: String? = null,
    var sentAt: LocalDateTime? = null
)