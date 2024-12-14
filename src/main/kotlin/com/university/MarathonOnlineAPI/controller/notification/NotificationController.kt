package com.university.MarathonOnlineAPI.controller.notification

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
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
            //logger.info("Show newNotification: $addedNotification")
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
            //logger.info("Show newNotification: $addedNotification")
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
            //logger.info("Show newNotification: $addedNotification")
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
            //logger.info("Show newNotification: $addedNotification")
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
            //logger.info("Show newNotification: $addedNotification")
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
            val notifications = notificationService.getNotifications()
            ResponseEntity(notifications, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getNotifications: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getNotificationById(@PathVariable id: Long): ResponseEntity<NotificationDTO> {
        return try {
            val foundNotification = notificationService.getById(id)
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
}
