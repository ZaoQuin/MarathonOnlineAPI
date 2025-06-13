package com.university.MarathonOnlineAPI.service.impl

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification as FCMNotification
import com.university.MarathonOnlineAPI.controller.notification.CreateAllNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateGroupNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.CreateIndividualNotificationRequest
import com.university.MarathonOnlineAPI.controller.notification.UpdateFCMTokenRequest
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.NotificationException
import com.university.MarathonOnlineAPI.mapper.NotificationMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.FCMTokenRepository
import com.university.MarathonOnlineAPI.repos.NotificationRepository
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.NotificationService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenRepository: FCMTokenRepository,
    private val notificationMapper: NotificationMapper,
    private val userMapper: UserMapper,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val firebaseMessaging: FirebaseMessaging,
    private val contestRepository: ContestRepository
) : NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)

    companion object {
        private const val KEY_NOTIFICATION_ID = "notification-id"
        private const val KEY_NOTIFICATION_TYPE = "notification-type"
        private const val KEY_OBJECT_ID = "object-id"
    }

    override fun addNotification(newNotification: NotificationDTO): NotificationDTO {
        return try {
            val notification = Notification(
                receiver = newNotification.receiver?.let { userMapper.toEntity(it) },
                objectId = newNotification.objectId,
                title = newNotification.title,
                content = newNotification.content,
                createAt = newNotification.createAt ?: LocalDateTime.now(),
                isRead = newNotification.isRead ?: false,
                type = newNotification.type
            )
            val saveNotification = notificationRepository.save(notification)
            val result = notificationMapper.toDto(saveNotification)

            sendPushNotification(result)

            result
        } catch (e: DataAccessException) {
            logger.error("Error saving notification: ${e.message}")
            throw NotificationException("Database error occurred while saving notification: ${e.message}")
        }
    }

    override fun addIndividualNotification(request: CreateIndividualNotificationRequest): NotificationDTO {
        return try {
            val notification = Notification(
                receiver = request.receiver?.let { userMapper.toEntity(it) },
                objectId = request.objectId,
                title = request.title,
                content = request.content,
                createAt = LocalDateTime.now(),
                isRead = false,
                type = request.type
            )
            val saveNotification = notificationRepository.save(notification)
            val result = notificationMapper.toDto(saveNotification)

            sendPushNotification(result)

            result
        } catch (e: DataAccessException) {
            logger.error("Error saving notification: ${e.message}")
            throw NotificationException("Database error occurred while saving notification: ${e.message}")
        }
    }

    override fun addAllRunnerNotification(request: CreateAllNotificationRequest): List<NotificationDTO> {
        return try {
            val runners = userRepository.findByRole(ERole.RUNNER)
            val notifications = mutableListOf<Notification>()

            runners.forEach { receiver ->
                val notification = Notification(
                    receiver = receiver,
                    objectId = request.objectId,
                    title = request.title,
                    content = request.content,
                    createAt = LocalDateTime.now(),
                    isRead = false,
                    type = request.type
                )
                notifications.add(notification)
            }

            val savedNotifications = notificationRepository.saveAll(notifications)
            val results = savedNotifications.map { notificationMapper.toDto(it) }

            results.forEach { sendPushNotification(it) }

            results
        } catch (e: DataAccessException) {
            logger.error("Error saving notification: ${e.message}")
            throw NotificationException("Database error occurred while saving notification: ${e.message}")
        }
    }

    override fun addGroupNotification(request: CreateGroupNotificationRequest): List<NotificationDTO> {
        return try {
            val notifications = mutableListOf<Notification>()

            request.receivers.forEach { receiver ->
                val notification = Notification(
                    receiver = userMapper.toEntity(receiver),
                    objectId = request.objectId,
                    title = request.title,
                    content = request.content,
                    createAt = LocalDateTime.now(),
                    isRead = false,
                    type = request.type
                )
                notifications.add(notification)
            }

            val savedNotifications = notificationRepository.saveAll(notifications)
            val results = savedNotifications.map { notificationMapper.toDto(it) }

            results.forEach { sendPushNotification(it) }

            results
        } catch (e: DataAccessException) {
            logger.error("Error saving notification: ${e.message}")
            throw NotificationException("Database error occurred while saving notification: ${e.message}")
        }
    }

    override fun readNotify(notificationDTO: NotificationDTO): NotificationDTO {
        return try {
            val existingNotification = notificationRepository.findById(
                notificationDTO.id ?: throw NotificationException("Notification ID must not be null")
            ).orElseThrow { NotificationException("Notification with ID ${notificationDTO.id} not found") }

            existingNotification.isRead = true
            val savedNotification = notificationRepository.save(existingNotification)
            notificationMapper.toDto(savedNotification)
        } catch (e: DataAccessException) {
            logger.error("Error updating notification: ${e.message}")
            throw NotificationException("Database error occurred while updating notification: ${e.message}")
        }
    }

    override fun deleteNotificationById(id: Long) {
        try {
            if (!notificationRepository.existsById(id)) {
                throw NotificationException("Notification with ID $id not found")
            }
            notificationRepository.deleteById(id)
            logger.info("Notification with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting notification with ID $id: ${e.message}")
            throw NotificationException("Database error occurred while deleting notification: ${e.message}")
        }
    }

    override fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO {
        return try {
            val existingNotification = notificationRepository.findById(
                notificationDTO.id ?: throw NotificationException("Notification ID must not be null")
            ).orElseThrow { NotificationException("Notification with ID ${notificationDTO.id} not found") }

            existingNotification.apply {
                receiver = notificationDTO.receiver?.let { userMapper.toEntity(it) }
                objectId = notificationDTO.objectId
                title = notificationDTO.title
                content = notificationDTO.content
                createAt = notificationDTO.createAt
                isRead = notificationDTO.isRead
                type = notificationDTO.type
            }

            val savedNotification = notificationRepository.save(existingNotification)
            notificationMapper.toDto(savedNotification)
        } catch (e: DataAccessException) {
            logger.error("Error updating notification: ${e.message}")
            throw NotificationException("Database error occurred while updating notification: ${e.message}")
        }
    }

    override fun getAllNotifications(): List<NotificationDTO> {
        return try {
            val notifications = notificationRepository.findAll()
            notifications.map { notificationMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving notifications: ${e.message}")
            throw NotificationException("Database error occurred while retrieving notifications: ${e.message}")
        }
    }

    override fun getNotificationById(id: Long): NotificationDTO {
        return try {
            val notification = notificationRepository.findById(id)
                .orElseThrow { NotificationException("Notification with ID $id not found") }
            notificationMapper.toDto(notification)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving notification with ID $id: ${e.message}")
            throw NotificationException("Database error occurred while retrieving notification: ${e.message}")
        }
    }

    override fun getNotificationsByJwt(jwt: String): List<NotificationDTO> {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val notifications = userDTO.id?.let {
                notificationRepository.getByReceiverId(it)
            } ?: emptyList()

            notifications.map { notificationMapper.toDto(it) }
                .sortedByDescending { it.createAt }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving notifications by JWT: ${e.message}")
            throw NotificationException("Database error occurred while retrieving notifications: ${e.message}")
        }
    }

    override fun updateFCMToken(request: UpdateFCMTokenRequest, jwt: String): FCMToken {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val user = userMapper.toEntity(userDTO)

            val existingToken = fcmTokenRepository.findByUserIdAndDeviceId(
                user.id!!, request.deviceId!!
            )

            val fcmToken = existingToken ?: FCMToken(
                user = user,
                deviceId = request.deviceId,
                createdAt = LocalDateTime.now()
            )

            fcmToken.apply {
                token = request.fcmToken
                deviceType = request.deviceType
                appVersion = request.appVersion
                updatedAt = LocalDateTime.now()
            }

            fcmTokenRepository.save(fcmToken)
        } catch (e: DataAccessException) {
            logger.error("Error updating FCM token: ${e.message}")
            throw NotificationException("Database error occurred while updating FCM token: ${e.message}")
        }
    }

    override fun markAllAsRead(jwt: String): List<NotificationDTO> {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val notifications = userDTO.id?.let {
                notificationRepository.getByReceiverId(it)
            } ?: emptyList()

            notifications.forEach { it.isRead = true }
            val updatedNotifications = notificationRepository.saveAll(notifications)

            updatedNotifications.map { notificationMapper.toDto(it) }
                .sortedByDescending { it.createAt }
        } catch (e: DataAccessException) {
            logger.error("Error marking all notifications as read: ${e.message}")
            throw NotificationException("Database error occurred while marking notifications as read: ${e.message}")
        }
    }

    override fun getUnreadCount(jwt: String): Int {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            userDTO.id?.let {
                notificationRepository.countByReceiverIdAndIsReadFalse(it)
            } ?: 0
        } catch (e: DataAccessException) {
            logger.error("Error getting unread count: ${e.message}")
            throw NotificationException("Database error occurred while getting unread count: ${e.message}")
        }
    }

    override fun sendPushNotification(notification: NotificationDTO) {
        try {
            val receiverId = notification.receiver?.id ?: return
            val fcmTokens = fcmTokenRepository.findByUserId(receiverId)

            if (fcmTokens.isEmpty()) {
                logger.info("No FCM tokens found for user $receiverId")
                return
            }

            fcmTokens.forEach { fcmToken ->
                try {
                    val messageBuilder = Message.builder()
                        .setToken(fcmToken.token)
                        .setNotification(
                            FCMNotification.builder()
                                .setTitle(notification.title ?: "Marathon Notification")
                                .setBody(notification.content ?: "")
                                .build()
                        )
                        .putData(KEY_NOTIFICATION_ID, notification.id.toString())
                        .putData(KEY_NOTIFICATION_TYPE, notification.type?.name ?: "")

                    notification.objectId?.let {
                        messageBuilder.putData(KEY_OBJECT_ID, it.toString())
                    }

                    val message = messageBuilder.build()
                    val response = firebaseMessaging.send(message)
                    logger.info("Push notification sent successfully: $response")
                } catch (e: Exception) {
                    logger.error("Failed to send push notification to token ${fcmToken.token}: ${e.message}")
                    if (e.message?.contains("registration-token-not-registered") == true) {
                        fcmTokenRepository.delete(fcmToken)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending push notification: ${e.message}")
        }
    }

    override fun sendPushNotificationToUser(userId: Long, title: String, content: String) {
        try {
            val fcmTokens = fcmTokenRepository.findByUserId(userId)

            fcmTokens.forEach { fcmToken ->
                try {
                    val message = Message.builder()
                        .setToken(fcmToken.token)
                        .setNotification(
                            FCMNotification.builder()
                                .setTitle(title)
                                .setBody(content)
                                .build()
                        )
                        .build()

                    val response = firebaseMessaging.send(message)
                    logger.info("Push notification sent successfully: $response")
                } catch (e: Exception) {
                    logger.error("Failed to send push notification: ${e.message}")
                    if (e.message?.contains("registration-token-not-registered") == true) {
                        fcmTokenRepository.delete(fcmToken)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending push notification to user: ${e.message}")
        }
    }

    override fun sendNotificationToRunners(contestId: Long, title: String, content: String) {
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest with ID $contestId not found") }
        val runners = userRepository.findAllByRole(ERole.RUNNER)
        val notifications = runners.map { runner ->
            Notification(
                receiver = runner,
                objectId = contest.id,
                title = title,
                content = content,
                createAt = LocalDateTime.now(),
                isRead = false,
                type = ENotificationType.NEW_CONTEST
            )
        }


        val savedNotifications = notificationRepository.saveAll(notifications)

        savedNotifications.map {
            sendPushNotification(notificationMapper.toDto(it))
        }
    }
}
