package com.university.MarathonOnlineAPI.controller.trainingFeedback

import com.university.MarathonOnlineAPI.dto.TrainingFeedbackDTO
import com.university.MarathonOnlineAPI.exception.TrainingPlanException
import com.university.MarathonOnlineAPI.service.TrainingFeedbackService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/training-feedback")
class TrainingFeedbackController(
    private val trainingFeedbackService: TrainingFeedbackService
) {
    private val logger = LoggerFactory.getLogger(TrainingFeedbackController::class.java)

    @PostMapping("/{trainingDayId}")
    fun submitFeedback(
        @PathVariable trainingDayId: Long,
        @RequestBody @Valid feedback: TrainingFeedbackDTO
    ): ResponseEntity<TrainingFeedbackDTO> {
        return try {
            val result = trainingFeedbackService.submitFeedback(trainingDayId, feedback)
            ResponseEntity.ok(result)
        } catch (e: TrainingPlanException) {
            logger.error("Training plan exception: ${e.message}")
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid enum value: ${e.message}")
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{trainingDayId}")
    fun getFeedback(
        @PathVariable trainingDayId: Long
    ): ResponseEntity<TrainingFeedbackDTO> {
        return try {
            val result = trainingFeedbackService.getFeedback(trainingDayId)
            ResponseEntity.ok(result)
        } catch (e: TrainingPlanException) {
            logger.error("Training plan exception: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}