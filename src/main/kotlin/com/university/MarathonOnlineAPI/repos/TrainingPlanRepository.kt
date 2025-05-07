package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TrainingPlanRepository : JpaRepository<TrainingPlan, Long> {
    fun findByUserId(userId: Long): List<TrainingPlan>
    fun findByUserIdOrderByCreatedAtDesc(id: Long): List<TrainingPlan>

    fun findByUserIdAndStatusAndStartDateBeforeAndEndDateAfter(
        userId: Long,
        status: ETrainingPlanStatus,
        start: LocalDateTime,
        end: LocalDateTime
    ): TrainingPlan?

    fun findByUserIdAndStatus(userId: Long, status: ETrainingPlanStatus): List<TrainingPlan>
}
