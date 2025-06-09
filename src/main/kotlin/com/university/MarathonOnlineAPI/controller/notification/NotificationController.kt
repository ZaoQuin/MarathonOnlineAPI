package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.exception.NotificationException
import com.university.MarathonOnlineAPI.service.NotificationService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notification")
class NotificationController(private val notificationService: NotificationService) {

    private val logger = LoggerFactory.getLogger(NotificationController::class.java)

    @PostMapping
    fun addNotification(@RequestBody @Valid notification: NotificationDTO): ResponseEntity<Any> {
        return try {
            val addedNotification = notificationService.addNotification(notification)
            ResponseEntity(addedNotification, HttpStatus.CREATED)
        } catch (e: NotificationException) {
            logger.error("Error adding notification: ${e.message}")
            ResponseEntity("Notification error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }


    @PutMapping("/readed")
    fun readNotify(@RequestBody @Valid notification: NotificationDTO): ResponseEntity<Any> {
        return try {
            val addedNotification = notificationService.readNotify(notification)
            ResponseEntity(addedNotification, HttpStatus.CREATED)
        } catch (e: NotificationException) {
            logger.error("Error adding notification: ${e.message}")
            ResponseEntity("Notification error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/individual")
    fun addIndividualNotification(@RequestBody @Valid notification: CreateIndividualNotificationRequest): ResponseEntity<Any> {
        return try {
            val addedNotification = notificationService.addIndividualNotification(notification)
            ResponseEntity(addedNotification, HttpStatus.CREATED)
        } catch (e: NotificationException) {
            logger.error("Error adding notification: ${e.message}")
            ResponseEntity("Notification error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/all")
    fun addAllRunnerNotification(@RequestBody @Valid notification: CreateAllNotificationRequest): ResponseEntity<Any> {
        return try {
            val addedNotification = notificationService.addAllRunnerNotification(notification)
            ResponseEntity(addedNotification, HttpStatus.CREATED)
        } catch (e: NotificationException) {
            logger.error("Error adding notification: ${e.message}")
            ResponseEntity("Notification error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/group")
    fun addGroupNotification(@RequestBody @Valid newNotification: CreateGroupNotificationRequest): ResponseEntity<Any> {
        return try {
            val addedNotification = notificationService.addGroupNotification(newNotification)
            ResponseEntity(addedNotification, HttpStatus.CREATED)
        } catch (e: NotificationException) {
            logger.error("Error adding notification: ${e.message}")
            ResponseEntity("Notification error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            notificationService.deleteNotificationById(id)
            logger.info("Notification with ID $id deleted successfully")
            ResponseEntity.ok("Notification with ID $id deleted successfully")
        } catch (e: NotificationException) {
            logger.error("Failed to delete Notification with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete Notification with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete Notification with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete Notification with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateNotification(@RequestBody @Valid notificationDTO: NotificationDTO): ResponseEntity<NotificationDTO> {
        return try {
            val updatedNotification = notificationService.updateNotification(notificationDTO)
            ResponseEntity(updatedNotification, HttpStatus.OK)
        } catch (e: NotificationException) {
            logger.error("Notification exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw NotificationException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating Notification: ${e.message}")
            throw NotificationException("Error updating notification: ${e.message}")
        }
    }

    @GetMapping
    fun getNotifications(): ResponseEntity<List<NotificationDTO>> {
        return try {
            val notifications = notificationService.getAllNotifications()
            ResponseEntity(notifications, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getNotifications: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getNotificationById(@PathVariable id: Long): ResponseEntity<NotificationDTO> {
        return try {
            val foundNotification = notificationService.getNotificationById(id)
            ResponseEntity.ok(foundNotification)
        } catch (e: NotificationException) {
            logger.error("Error getting notification by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting notification by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/my-notify")
    fun getNotificationsByJwt(@RequestHeader("Authorization") token: String): ResponseEntity<List<NotificationDTO>> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val foundNotification = notificationService.getNotificationsByJwt(jwt)
            ResponseEntity.ok(foundNotification)
        } catch (e: NotificationException) {
            logger.error("Error getting notification by JWT")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting notification by JWT")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/fcm-token")
    fun updateFCMToken(
        @RequestBody @Valid request: UpdateFCMTokenRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val result = notificationService.updateFCMToken(request, jwt)
            logger.info("FCM token updated successfully for device: ${request.deviceId}")
            ResponseEntity.ok(result)
        } catch (e: NotificationException) {
            logger.error("Error updating FCM token: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("FCM token update failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating FCM token: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred: ${e.message}")
        }
    }

    /**
     * API để đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/mark-all-read")
    fun markAllAsRead(@RequestHeader("Authorization") token: String): ResponseEntity<List<NotificationDTO>> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val updatedNotifications = notificationService.markAllAsRead(jwt)
            logger.info("All notifications marked as read for user")
            ResponseEntity.ok(updatedNotifications)
        } catch (e: NotificationException) {
            logger.error("Error marking all notifications as read: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: Exception) {
            logger.error("Error marking all notifications as read: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * API để lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    fun getUnreadCount(@RequestHeader("Authorization") token: String): ResponseEntity<Int> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val unreadCount = notificationService.getUnreadCount(jwt)
            ResponseEntity.ok(unreadCount)
        } catch (e: NotificationException) {
            logger.error("Error getting unread count: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: Exception) {
            logger.error("Error getting unread count: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
