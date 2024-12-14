package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.notification.CreateAllNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateGroupNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateIndividualNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateNotificationRequest
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.UserDTO

interface NotificationService {
    fun addNotification(newNotification: NotificationDTO): NotificationDTO
    fun deleteNotificationById(id: Long)
    fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO
    fun getNotifications(): List<NotificationDTO>
    fun getById(id: Long): NotificationDTO
    fun getNotificationsByJwt(jwt: String): List<NotificationDTO>
    fun addIndividualNotification(notification: CreateIndividualNotificationRequest): NotificationDTO
    fun addAllRunnerNotification(notification: CreateAllNotificationRequest): List<NotificationDTO>
    fun addGroupNotification(newNotification: CreateGroupNotificationRequest): List<NotificationDTO>
    fun readNotify(notification: NotificationDTO): NotificationDTO
}