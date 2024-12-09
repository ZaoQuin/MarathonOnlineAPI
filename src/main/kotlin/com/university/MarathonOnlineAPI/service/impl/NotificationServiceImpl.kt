package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.Notification
import com.university.MarathonOnlineAPI.entity.User
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.NotificationException
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.exception.RuleException
import com.university.MarathonOnlineAPI.mapper.ContestMapper
import com.university.MarathonOnlineAPI.mapper.NotificationMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.NotificationRepository
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.NotificationService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val notificationMapper: NotificationMapper,
    private val userMapper: UserMapper,
    private val contestMapper: ContestMapper,
    private val contestRepository: ContestRepository,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val userService: UserService
) : NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)

    override fun addNotification(newNotification: NotificationDTO): NotificationDTO {
        return try {
            val notification = Notification(
                receiver = newNotification.receiver?.let { userMapper.toEntity(it) },
                contest = newNotification.contest?.let { contestMapper.toEntity(it) },
                title = newNotification.title,
                content = newNotification.content,
                createAt =  newNotification.createAt,
                isRead = newNotification.isRead,
                type =  newNotification.type
            )
            val saveNotification = notificationRepository.save(notification)
            notificationMapper.toDto(saveNotification)
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
            val existingNotification = notificationRepository.findById(notificationDTO.id ?: throw RuleException("Rule ID must not be null"))
                .orElseThrow { RuleException("Rule with ID ${notificationDTO.id} not found") }
            existingNotification.receiver = User(1)
            existingNotification.contest = Contest(32)
            existingNotification.title = notificationDTO.title
            existingNotification.content = notificationDTO.content
            existingNotification.createAt = notificationDTO.createAt
            existingNotification.isRead = notificationDTO.isRead
            existingNotification.type = notificationDTO.type

            notificationRepository.save(existingNotification)
            notificationMapper.toDto(existingNotification)
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

    override fun getNotificationsByJwt(jwt: String): List<NotificationDTO> {
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val notifications = userDTO.id?.let { notificationRepository.getByReceiverId(it) }
            return notifications?.map { notificationMapper.toDto(it) }!!
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RaceException("Database error occurred while saving race: ${e.message}")
        }
    }
}
