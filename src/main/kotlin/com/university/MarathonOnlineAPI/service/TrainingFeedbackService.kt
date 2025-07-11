package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingFeedbackDTO

interface TrainingFeedbackService {
    fun submitFeedback(trainingDayId: Long, feedback: TrainingFeedbackDTO): TrainingFeedbackDTO
    fun getFeedback(trainingDayId: Long): TrainingFeedbackDTO
}