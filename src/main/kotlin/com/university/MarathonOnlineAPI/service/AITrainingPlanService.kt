package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.entity.TrainingDay
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput
import java.time.LocalDateTime

interface AITrainingPlanService {
    fun generateTrainingDayForDate(input: TrainingPlanInput, plan: TrainingPlan, date: LocalDateTime): TrainingDay
}