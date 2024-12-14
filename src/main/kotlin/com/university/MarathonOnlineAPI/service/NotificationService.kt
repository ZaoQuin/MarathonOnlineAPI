package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.Contest

interface NotificationService {
    fun addNotification(newNotification: NotificationDTO): NotificationDTO
    fun deleteNotificationById(id: Long)
    fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO
    fun getNotifications(): List<NotificationDTO>
    fun getById(id: Long): NotificationDTO
    fun getNotificationsByJwt(jwt: String): List<NotificationDTO>
    fun sendNotificationToRunners(contestId: Long, title: String, content: String)
}