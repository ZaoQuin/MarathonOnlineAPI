package com.university.MarathonOnlineAPI.controller.feedback

import com.university.MarathonOnlineAPI.controller.StringResponse
import com.university.MarathonOnlineAPI.dto.FeedbackDTO
import com.university.MarathonOnlineAPI.exception.FeedbackException
import com.university.MarathonOnlineAPI.service.FeedbackService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/feedback")
class FeedbackController(private val feedbackService: FeedbackService) {

    private val logger = LoggerFactory.getLogger(FeedbackController::class.java)

    @PostMapping("/record/{recordId}")
    fun createRecordFeedback(
        @PathVariable recordId: Long,
        @RequestBody @Valid request: CreateFeedbackRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        return try {
            val jwt = authorization.replace("Bearer ", "")
            val feedback = feedbackService.createRecordFeedback(recordId, request.message, jwt)

            logger.info("Feedback created successfully for record ID: $recordId")
            ResponseEntity(feedback, HttpStatus.CREATED)
        } catch (e: FeedbackException) {
            logger.error("Error creating feedback: ${e.message}")
            ResponseEntity("Feedback error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/record/{recordId}")
    fun getFeedbacksByRecord(@PathVariable recordId: Long): ResponseEntity<List<FeedbackDTO>> {
        return try {
            val feedbacks = feedbackService.getFeedbacksByRecordId(recordId)
            ResponseEntity(feedbacks, HttpStatus.OK)
        } catch (e: FeedbackException) {
            logger.error("Error retrieving feedbacks for record ID $recordId: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error retrieving feedbacks for record ID $recordId: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/approval/{approvalId}")
    fun getFeedbacksByApproval(@PathVariable approvalId: Long): ResponseEntity<List<FeedbackDTO>> {
        return try {
            val feedbacks = feedbackService.getFeedbacksByApprovalId(approvalId)
            ResponseEntity(feedbacks, HttpStatus.OK)
        } catch (e: FeedbackException) {
            logger.error("Error retrieving feedbacks for approval ID $approvalId: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error retrieving feedbacks for approval ID $approvalId: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/{feedbackId}")
    fun deleteFeedback(
        @PathVariable feedbackId: Long,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StringResponse> {
        return try {
            val jwt = authorization.replace("Bearer ", "")
            feedbackService.deleteFeedback(feedbackId, jwt)

            logger.info("Feedback with ID $feedbackId deleted successfully")
            ResponseEntity.ok(StringResponse("Feedback with ID $feedbackId deleted successfully"))
        } catch (e: FeedbackException) {
            logger.error("Failed to delete feedback with ID $feedbackId: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StringResponse("Failed to delete feedback with ID $feedbackId: ${e.message}"))
        } catch (e: Exception) {
            logger.error("Failed to delete feedback with ID $feedbackId: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StringResponse("Failed to delete feedback with ID $feedbackId: ${e.message}"))
        }
    }

    @PutMapping
    fun updateFeedback(@RequestBody @Valid feedbackDTO: FeedbackDTO): ResponseEntity<FeedbackDTO> {
        return try {
            val updatedFeedback = feedbackService.updateFeedback(feedbackDTO)
            ResponseEntity(updatedFeedback, HttpStatus.OK)
        } catch (e: FeedbackException) {
            logger.error("Feedback exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw FeedbackException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating feedback: ${e.message}")
            throw FeedbackException("Error updating feedback: ${e.message}")
        }
    }

    @GetMapping
    fun getAllFeedbacks(): ResponseEntity<List<FeedbackDTO>> {
        return try {
            val feedbacks = feedbackService.getAllFeedbacks()
            ResponseEntity(feedbacks, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getAllFeedbacks: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getFeedbackById(@PathVariable id: Long): ResponseEntity<FeedbackDTO> {
        return try {
            val foundFeedback = feedbackService.getFeedbackById(id)
            ResponseEntity.ok(foundFeedback)
        } catch (e: FeedbackException) {
            logger.error("Error getting feedback by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting feedback by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/my-feedbacks")
    fun getFeedbacksByJwt(@RequestHeader("Authorization") token: String): ResponseEntity<List<FeedbackDTO>> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val foundFeedbacks = feedbackService.getFeedbacksByJwt(jwt)
            ResponseEntity.ok(foundFeedbacks)
        } catch (e: FeedbackException) {
            logger.error("Error getting feedbacks by JWT")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting feedbacks by JWT")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/registration/{registrationId}")
    fun createRegistrationFeedback(
        @PathVariable registrationId: Long,
        @RequestBody @Valid request: CreateFeedbackRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        return try {
            val jwt = authorization.replace("Bearer ", "")
            val feedback = feedbackService.createRegistrationFeedback(registrationId, request.message, jwt)

            logger.info("Feedback created successfully for record ID: $registrationId")
            ResponseEntity(feedback, HttpStatus.CREATED)
        } catch (e: FeedbackException) {
            logger.error("Error creating feedback: ${e.message}")
            ResponseEntity("Feedback error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/registration/{registrationId}")
    fun getFeedbacksByRegistration(@PathVariable registrationId: Long): ResponseEntity<List<FeedbackDTO>> {
        return try {
            val feedbacks = feedbackService.getFeedbacksByRegistrationId(registrationId)
            ResponseEntity(feedbacks, HttpStatus.OK)
        } catch (e: FeedbackException) {
            logger.error("Error retrieving feedbacks for registration ID $registrationId: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error retrieving feedbacks for registration ID $registrationId: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}