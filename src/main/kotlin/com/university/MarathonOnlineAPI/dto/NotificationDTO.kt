package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ENotificationType
import java.time.LocalDateTime

data class NotificationDTO(
    var id: Long? = null,
    var receiver: UserDTO? = null,
    var objectId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var createAt: LocalDateTime? = null,
    var isRead: Boolean? = null,
    var type: ENotificationType? = null
)
