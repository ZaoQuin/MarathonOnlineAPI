package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO

interface TrainingPlanService {
    fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, jwt: String): TrainingPlanDTO
    fun getUserTrainingPlans(userId: Long): List<TrainingPlanDTO>
    fun getTrainingPlanById(planId: Long): TrainingPlanDTO
    fun getTrainingPlanByJwt(jwt: String): TrainingPlanDTO
}