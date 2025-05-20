package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import com.university.MarathonOnlineAPI.view.SingleTrainingPlanView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TrainingPlanRepository : JpaRepository<TrainingPlan, Long> {
    fun findByUserId(userId: Long): List<TrainingPlan>
    fun findByUserIdOrderByCreatedAtDesc(id: Long): List<TrainingPlan>

    @Query("""
        SELECT tp FROM TrainingPlan tp 
        WHERE tp.user.id = :userId 
          AND tp.status = :status 
          AND tp.startDate <= :now 
          AND tp.endDate >= :now
    """)
    fun findActivePlanNow(
        @Param("userId") userId: Long,
        @Param("status") status: ETrainingPlanStatus,
        @Param("now") now: LocalDateTime
    ): TrainingPlan?

    fun findByUserIdAndStatusAndStartDateBeforeAndEndDateAfter(
        userId: Long,
        status: ETrainingPlanStatus,
        start: LocalDateTime,
        end: LocalDateTime
    ): TrainingPlan?

    fun findByUserIdAndStatus(userId: Long, status: ETrainingPlanStatus): List<TrainingPlan>
    fun findByUserIdAndStatus(
        userId: Long,
        status: ETrainingPlanStatus,
        pageable: Pageable
    ): Page<SingleTrainingPlanView>

    @Query("""
    SELECT 
        tp.id AS id, 
        tp.name AS name, 
        tp.startDate AS startDate, 
        tp.endDate AS endDate, 
        tp.status AS status 
    FROM TrainingPlan tp 
    WHERE 
        tp.user.id = :userId AND 
        tp.status = :status AND 
        (:startDate IS NULL OR tp.endDate  >= :startDate) AND 
        (:endDate IS NULL OR tp.startDate <= :endDate)
""")
    fun findProjectedByFilters(
        @Param("userId") userId: Long,
        @Param("status") status: ETrainingPlanStatus,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<SingleTrainingPlanView>

    fun findTopByUserIdAndStatusOrderByStartDateDesc(userId: Long, status: ETrainingPlanStatus = ETrainingPlanStatus.ACTIVE): TrainingPlan?
}
