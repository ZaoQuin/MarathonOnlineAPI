package com.university.MarathonOnlineAPI.controller.notification

import jakarta.validation.constraints.NotBlank

data class UpdateFCMTokenRequest(
    @field:NotBlank(message = "FCM token cannot be blank")
    var fcmToken: String? = null,

    @field:NotBlank(message = "Device type cannot be blank")
    var deviceType: String? = null,

    @field:NotBlank(message = "Device ID cannot be blank")
    var deviceId: String? = null,

    var appVersion: String? = null
)