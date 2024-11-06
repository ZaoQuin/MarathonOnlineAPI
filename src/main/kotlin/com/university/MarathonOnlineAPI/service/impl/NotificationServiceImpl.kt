package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.exception.NotificationException
import com.university.MarathonOnlineAPI.mapper.NotificationMapper
import com.university.MarathonOnlineAPI.repos.NotificationRepository
import com.university.MarathonOnlineAPI.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val notificationMapper: NotificationMapper
) : NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)

    override fun addNotification(newNotification: NotificationDTO): NotificationDTO {
        logger.info("Received NotificationDTO: $newNotification")
        return try {
            val notificationEntity = notificationMapper.toEntity(newNotification)
            val savedNotification = notificationRepository.save(notificationEntity)
            notificationMapper.toDto(savedNotification)
        } catch (e: DataAccessException) {
            logger.error("Error saving notification: ${e.message}")
            throw NotificationException("Database error occurred while saving notification: ${e.message}")
        }
    }

    override fun deleteNotificationById(id: Long) {
        try {
            notificationRepository.deleteById(id)
            logger.info("Notification with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting notification with ID $id: ${e.message}")
            throw NotificationException("Database error occurred while deleting notification: ${e.message}")
        }
    }

    override fun updateNotification(notificationDTO: NotificationDTO): NotificationDTO {
        return try {
            val notificationEntity = notificationMapper.toEntity(notificationDTO)
            val updatedNotification = notificationRepository.save(notificationEntity)
            notificationMapper.toDto(updatedNotification)
        } catch (e: DataAccessException) {
            logger.error("Error updating notification: ${e.message}")
            throw NotificationException("Database error occurred while updating notification: ${e.message}")
        }
    }

    override fun getNotifications(): List<NotificationDTO> {
        return try {
            val notifications = notificationRepository.findAll()
            notifications.map { notificationMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving notifications: ${e.message}")
            throw NotificationException("Database error occurred while retrieving notifications: ${e.message}")
        }
    }

    override fun getById(id: Long): NotificationDTO {
        return try {
            val notification = notificationRepository.findById(id)
                .orElseThrow { NotificationException("Notification with ID $id not found") }
            notificationMapper.toDto(notification)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving notification with ID $id: ${e.message}")
            throw NotificationException("Database error occurred while retrieving notification: ${e.message}")
        }
    }
}
