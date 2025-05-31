package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateIndividualNotificationRequest(
    @field:NotNull(message = "Receiver cannot be null")
    var receiver: UserDTO? = null,

    var contest: ContestDTO? = null,

    @field:NotBlank(message = "Title cannot be blank")
    var title: String? = null,

    @field:NotBlank(message = "Content cannot be blank")
    var content: String? = null,

    @field:NotNull(message = "Type cannot be null")
    var type: ENotificationType? = null
)