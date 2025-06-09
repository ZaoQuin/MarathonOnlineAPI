package com.university.MarathonOnlineAPI.service.impl

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification as FCMNotification
import com.university.MarathonOnlineAPI.dto.FeedbackDTO
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.FeedbackException
import com.university.MarathonOnlineAPI.mapper.FeedbackMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.FeedbackService
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
class FeedbackServiceImpl(
    private val feedbackRepository: FeedbackRepository,
    private val recordRepository: RecordRepository,
    private val fcmTokenRepository: FCMTokenRepository,
    private val userRepository: UserRepository,
    private val feedbackMapper: FeedbackMapper,
    private val userMapper: UserMapper,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val firebaseMessaging: FirebaseMessaging
) : FeedbackService {

    private val logger = LoggerFactory.getLogger(FeedbackServiceImpl::class.java)

    override fun createFeedback(
        recordId: Long,
        message: String,
        jwt: String
    ): FeedbackDTO {
        return try {
            val senderDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val sender = userMapper.toEntity(senderDTO)

            val record = recordRepository.findById(recordId)
                .orElseThrow { FeedbackException("Record with ID $recordId not found") }

            val approval = record.approval
                ?: throw FeedbackException("No approval found for record $recordId")

            val feedback = Feedback(
                sender = sender,
                message = message,
                sentAt = LocalDateTime.now(),
                approval = approval
            )

            val savedFeedback = feedbackRepository.save(feedback)
            val feedbackDTO = feedbackMapper.toDto(savedFeedback)

            sendFeedbackNotifications(feedbackDTO, record, senderDTO)

            feedbackDTO
        } catch (e: DataAccessException) {
            logger.error("Error creating feedback: ${e.message}")
            throw FeedbackException("Database error occurred while creating feedback: ${e.message}")
        }
    }

    override fun getFeedbacksByRecordId(recordId: Long): List<FeedbackDTO> {
        return try {
            val record = recordRepository.findById(recordId)
                .orElseThrow { FeedbackException("Record with ID $recordId not found") }

            val approvalId = record.approval?.id
                ?: throw FeedbackException("No approval found for record $recordId")

            val feedbacks = feedbackRepository.findByApprovalIdOrderBySentAtDesc(approvalId)
            feedbacks.map { feedbackMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving feedbacks for record $recordId: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedbacks: ${e.message}")
        }
    }

    override fun getFeedbacksByApprovalId(approvalId: Long): List<FeedbackDTO> {
        return try {
            val feedbacks = feedbackRepository.findByApprovalIdOrderBySentAtDesc(approvalId)
            feedbacks.map { feedbackMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving feedbacks for approval $approvalId: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedbacks: ${e.message}")
        }
    }

    override fun deleteFeedback(feedbackId: Long, jwt: String) {
        try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow { FeedbackException("Feedback with ID $feedbackId not found") }

            if (feedback.sender.id != userDTO.id && userDTO.role != ERole.ADMIN) {
                throw FeedbackException("You don't have permission to delete this feedback")
            }

            feedbackRepository.deleteById(feedbackId)
            logger.info("Feedback with ID $feedbackId deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting feedback with ID $feedbackId: ${e.message}")
            throw FeedbackException("Database error occurred while deleting feedback: ${e.message}")
        }
    }

    private fun sendFeedbackNotifications(
        feedback: FeedbackDTO,
        record: Record,
        sender: UserDTO
    ) {
        try {
            val isAdminSender = sender.role == ERole.ADMIN
            val runner = record.user!!

            if (isAdminSender) {
                val content = "Admin đã phản hồi về record của bạn: ${feedback.message?.take(100)}"
                createNotificationAndSendPush(
                    receiver = userMapper.toDto(runner),
                    title = "Phản hồi từ Admin",
                    content = content,
                    objectId = record.id,
                    type = ENotificationType.RECORD_FEEDBACK,
                    pushTitle = "Phản hồi về Record",
                    pushContent = content,
                    data = mapOf(
                        "feedbackId" to feedback.id.toString(),
                        "recordId" to record.id.toString(),
                        "type" to "ADMIN_FEEDBACK"
                    )
                )
            } else {

                val content = "${sender.fullName} đã gửi phản hồi về record: ${feedback.message?.take(100)}"
                val admins = userRepository.findByRole(ERole.ADMIN)

                admins.forEach { admin ->
                    createNotificationAndSendPush(
                        receiver = userMapper.toDto(admin),
                        title = "Phản hồi mới từ Runner",
                        content = content,
                        objectId = record.id,
                        type = ENotificationType.RECORD_FEEDBACK,
                        pushTitle = "Phản hồi mới",
                        pushContent = content,
                        data = mapOf(
                            "feedbackId" to feedback.id.toString(),
                            "recordId" to record.id.toString(),
                            "runnerId" to sender.id.toString(),
                            "type" to "RUNNER_FEEDBACK"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending feedback notifications: ${e.message}")
        }
    }

    private fun createNotificationAndSendPush(
        receiver: UserDTO,
        title: String,
        content: String,
        objectId: Long?,
        type: ENotificationType,
        pushTitle: String,
        pushContent: String,
        data: Map<String, String>
    ) {
        try {
            val notificationDTO = NotificationDTO(
                receiver = receiver,
                objectId = objectId,
                title = title,
                content = content,
                createAt = LocalDateTime.now(),
                isRead = false,
                type = type
            )

            val savedNotification = notificationService.addNotification(notificationDTO)

            sendFirebasePushNotification(
                userId = receiver.id!!,
                title = pushTitle,
                content = pushContent,
                data = data + mapOf("notificationId" to savedNotification.id.toString())
            )

        } catch (e: Exception) {
            logger.error("Error creating notification and sending push: ${e.message}")
        }
    }

    private fun sendFirebasePushNotification(
        userId: Long,
        title: String,
        content: String,
        data: Map<String, String>
    ) {
        try {
            val fcmTokens = fcmTokenRepository.findByUserId(userId)

            if (fcmTokens.isEmpty()) {
                logger.info("No FCM tokens found for user $userId")
                return
            }

            fcmTokens.forEach { fcmToken ->
                try {
                    val messageBuilder = Message.builder()
                        .setToken(fcmToken.token)
                        .setNotification(
                            FCMNotification.builder()
                                .setTitle(title)
                                .setBody(content)
                                .build()
                        )

                    data.forEach { (key, value) ->
                        messageBuilder.putData(key, value)
                    }

                    val message = messageBuilder.build()
                    val response = firebaseMessaging.send(message)

                    logger.info("Firebase feedback notification sent successfully: $response")
                } catch (e: Exception) {
                    logger.error("Failed to send Firebase notification to token ${fcmToken.token}: ${e.message}")

                    if (e.message?.contains("registration-token-not-registered") == true ||
                        e.message?.contains("invalid-registration-token") == true) {
                        fcmTokenRepository.delete(fcmToken)
                        logger.info("Removed invalid FCM token for user $userId")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending Firebase notification to user $userId: ${e.message}")
        }
    }

    override fun updateFeedback(feedbackDTO: FeedbackDTO): FeedbackDTO {
        return try {
            val existingFeedback = feedbackRepository.findById(feedbackDTO.id!!)
                .orElseThrow { FeedbackException("Feedback with ID ${feedbackDTO.id} not found") }

            val updatedFeedback = existingFeedback.copy(
                message = feedbackDTO.message ?: existingFeedback.message
            )

            val savedFeedback = feedbackRepository.save(updatedFeedback)
            feedbackMapper.toDto(savedFeedback)
        } catch (e: DataAccessException) {
            logger.error("Error updating feedback: ${e.message}")
            throw FeedbackException("Database error occurred while updating feedback: ${e.message}")
        }
    }

    override fun getAllFeedbacks(): List<FeedbackDTO> {
        return try {
            val feedbacks = feedbackRepository.findAll().sortedByDescending { it.sentAt }
            feedbacks.map { feedbackMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving all feedbacks: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedbacks: ${e.message}")
        }
    }

    override fun getById(id: Long): FeedbackDTO {
        return try {
            val feedback = feedbackRepository.findById(id)
                .orElseThrow { FeedbackException("Feedback with ID $id not found") }
            feedbackMapper.toDto(feedback)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving feedback by ID $id: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedback: ${e.message}")
        }
    }

    override fun getFeedbacksByJwt(jwt: String): List<FeedbackDTO> {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            val feedbacks = feedbackRepository.findBySenderIdOrderBySentAtDesc(userDTO.id!!)
            feedbacks.map { feedbackMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving feedbacks by JWT: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedbacks: ${e.message}")
        }
    }

    override fun getFeedbackCount(approvalId: Long): Long {
        return try {
            feedbackRepository.countByApprovalId(approvalId)
        } catch (e: DataAccessException) {
            logger.error("Error counting feedbacks for approval $approvalId: ${e.message}")
            0L
        }
    }

    override fun getFeedbacksByApprovalIds(approvalIds: List<Long>): List<FeedbackDTO> {
        return try {
            val feedbacks = feedbackRepository.findByApprovalIdsOrderBySentAtDesc(approvalIds)
            feedbacks.map { feedbackMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving feedbacks by approval IDs: ${e.message}")
            throw FeedbackException("Database error occurred while retrieving feedbacks: ${e.message}")
        }
    }
}