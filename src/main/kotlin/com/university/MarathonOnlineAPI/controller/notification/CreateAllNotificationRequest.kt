package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType

data class CreateAllNotificationRequest(
    var contest: ContestDTO? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null
)