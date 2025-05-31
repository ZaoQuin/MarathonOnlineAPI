package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateGroupNotificationRequest(
    @field:NotEmpty(message = "Receivers list cannot be empty")
    var receivers: List<UserDTO> = emptyList(),

    var contest: ContestDTO? = null,

    @field:NotBlank(message = "Title cannot be blank")
    var title: String? = null,

    @field:NotBlank(message = "Content cannot be blank")
    var content: String? = null,

    @field:NotNull(message = "Type cannot be null")
    var type: ENotificationType? = null
)