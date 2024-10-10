package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ENotificationType
import java.time.LocalDateTime

data class NotificationDTO(
    val id: Long? = null,
    val receiver: UserDTO? = null,
    val contest: ContestDTO? = null,
    val title: String? = null,
    val content: String? = null,
    val createAt: LocalDateTime? = null,
    val isRead: Boolean? = null,
    val type: ENotificationType? = null
)
