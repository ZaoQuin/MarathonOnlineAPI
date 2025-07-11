package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.view.SingleTrainingPlanView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface TrainingPlanService {
    fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, jwt: String): TrainingPlanDTO
    fun getUserTrainingPlans(userId: Long): List<TrainingPlanDTO>
    fun getTrainingPlanById(planId: Long): TrainingPlanDTO
    fun getTrainingPlanByJwt(jwt: String): TrainingPlanDTO
    fun getPlansByStatus(pageable: Pageable, status: ETrainingPlanStatus, jwt: String, startDate: LocalDateTime?, endDate: LocalDateTime?): Page<SingleTrainingPlanView>
}