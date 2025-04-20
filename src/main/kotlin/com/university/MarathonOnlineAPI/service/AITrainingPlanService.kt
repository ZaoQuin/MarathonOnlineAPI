package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.entity.TrainingDay
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput

interface AITrainingPlanService {
    fun generateTrainingDays(input: TrainingPlanInput, plan: TrainingPlan): List<TrainingDay>
}