package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput

interface TrainingPlanService {
    fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, userId: Long): TrainingPlanDTO
    fun getUserTrainingPlans(userId: Long): List<TrainingPlanDTO>
    fun getTrainingPlanById(planId: Long): TrainingPlanDTO
    fun getTrainingPlanByJwt(jwt: String): TrainingPlanDTO
}