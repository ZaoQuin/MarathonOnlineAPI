package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.TrainingFeedbackDTO
import com.university.MarathonOnlineAPI.entity.TrainingFeedback
import com.university.MarathonOnlineAPI.exception.TrainingPlanException
import com.university.MarathonOnlineAPI.mapper.TrainingFeedbackMapper
import com.university.MarathonOnlineAPI.repos.TrainingDayRepository
import com.university.MarathonOnlineAPI.repos.TrainingFeedbackRepository
import com.university.MarathonOnlineAPI.service.TrainingFeedbackService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TrainingFeedbackServiceImpl(
    private val trainingDayRepository: TrainingDayRepository,
    private val trainingFeedbackRepository: TrainingFeedbackRepository,
    private val trainingFeedbackMapper: TrainingFeedbackMapper
): TrainingFeedbackService {
    @Transactional
    override fun submitFeedback(trainingDayId: Long, feedbackDTO: TrainingFeedbackDTO): TrainingFeedbackDTO {
        // Verify training day belongs to user
        val trainingDay = trainingDayRepository.findById(trainingDayId)
            .orElseThrow { TrainingPlanException("Training day not found") }

        // Check if feedback already exists
        val existingFeedback = trainingFeedbackRepository.findByTrainingDayId(trainingDayId)

        val feedback = if (existingFeedback.isPresent) {
            // Update existing feedback
            existingFeedback.get().apply {
                difficultyRating = feedbackDTO.difficultyRating!!
                feelingRating = feedbackDTO.feelingRating!!
                notes = feedbackDTO.notes
            }
        } else {
            // Create new feedback
            TrainingFeedback(
                trainingDay = trainingDay,
                difficultyRating = feedbackDTO.difficultyRating!!,
                feelingRating = feedbackDTO.feelingRating!!,
                notes = feedbackDTO.notes
            )
        }

        val savedFeedback = trainingFeedbackRepository.save(feedback)
        return trainingFeedbackMapper.toDto(savedFeedback)
    }

    override fun getFeedback(trainingDayId: Long): TrainingFeedbackDTO {
        val trainingDay = trainingDayRepository.findById(trainingDayId)
            .orElseThrow { TrainingPlanException("Training day not found") }

        val feedback = trainingFeedbackRepository.findByTrainingDayId(trainingDayId)
            .orElseThrow { TrainingPlanException("Training feedback not found") }

        return trainingFeedbackMapper.toDto(feedback)
    }
}