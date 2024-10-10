package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ENotificationType
import java.util.*

data class NotificationDTO(
    val id: Long? = null,
    val receiver: UserDTO? = null,
    val contest: ContestDTO? = null,
    val title: String? = null,
    val content: String? = null,
    val createAt: Date? = null,
    val isRead: Boolean? = null,
    val type: ENotificationType? = null
)
