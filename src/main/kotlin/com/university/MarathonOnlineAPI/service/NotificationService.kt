package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.notification.CreateAllNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateGroupNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateIndividualNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.UpdateFCMTokenRequest
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.FCMToken

interface NotificationService {
    fun addNotification(newNotification: NotificationDTO): NotificationDTO
    fun deleteNotificationById(id: Long)
    fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO
    fun getNotificationsByJwt(jwt: String): List<NotificationDTO>
    fun addIndividualNotification(notification: CreateIndividualNotificationRequest): NotificationDTO
    fun addAllRunnerNotification(notification: CreateAllNotificationRequest): List<NotificationDTO>
    fun addGroupNotification(newNotification: CreateGroupNotificationRequest): List<NotificationDTO>
    fun readNotify(notification: NotificationDTO): NotificationDTO
    fun updateFCMToken(request: UpdateFCMTokenRequest, jwt: String): FCMToken
    fun markAllAsRead(jwt: String): List<NotificationDTO>
    fun getUnreadCount(jwt: String): Int
    fun sendPushNotification(notification: NotificationDTO)
    fun sendPushNotificationToUser(userId: Long, title: String, content: String)
    fun getAllNotifications(): List<NotificationDTO>
    fun getNotificationById(id: Long): NotificationDTO
}