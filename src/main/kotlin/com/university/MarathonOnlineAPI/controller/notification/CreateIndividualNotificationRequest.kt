package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType

data class CreateIndividualNotificationRequest(
    var contest: ContestDTO? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null,
    var receiver: UserDTO? = null
)