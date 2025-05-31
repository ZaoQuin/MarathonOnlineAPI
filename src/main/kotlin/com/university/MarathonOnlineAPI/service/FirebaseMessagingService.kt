package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.User

interface FirebaseMessagingService {
    fun sendNotificationToUser(
        user: User,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Boolean
    fun sendNotificationToMultipleUsers(
        users: List<User>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Int
    fun sendNotificationToAllUsers(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Int
    fun sendNotificationToTopic(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Boolean
    fun subscribeToTopic(fcmTokens: List<String>, topic: String): Boolean
    fun unsubscribeFromTopic(fcmTokens: List<String>, topic: String): Boolean
}