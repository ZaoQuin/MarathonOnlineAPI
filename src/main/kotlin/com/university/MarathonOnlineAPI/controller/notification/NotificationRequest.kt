package com.university.MarathonOnlineAPI.controller.notification

data class NotificationRequest(
    val contestId: Long,
    val title: String,
    val content: String,
    val type: String,
    val targetRole: String,
    val userId: Long
)
