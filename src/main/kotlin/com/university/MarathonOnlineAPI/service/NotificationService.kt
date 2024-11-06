package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.NotificationDTO

interface NotificationService {
    fun addNotification(newRule: NotificationDTO): NotificationDTO
    fun deleteNotificationById(id: Long)
    fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO
    fun getNotifications(): List<NotificationDTO>
    fun getById(id: Long): NotificationDTO
}