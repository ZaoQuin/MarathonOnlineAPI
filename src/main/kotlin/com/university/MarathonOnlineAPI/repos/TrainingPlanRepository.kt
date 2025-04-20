package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.TrainingPlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingPlanRepository : JpaRepository<TrainingPlan, Long> {
    fun findByUserId(userId: Long): List<TrainingPlan>
    fun findByUserIdOrderByCreatedAtDesc(id: Long): List<TrainingPlan>
}
