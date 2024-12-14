package com.university.MarathonOnlineAPI.controller.notification

data class NotificationRequest(
    val contestId: Long,        // ID của cuộc thi liên quan
    val title: String,          // Tiêu đề thông báo
    val content: String,        // Nội dung thông báo
    val type: String,           // Loại thông báo (dựa trên ENotificationType)
    val targetRole: String      // Vai trò của người nhận (ví dụ: RUNNER)
)
