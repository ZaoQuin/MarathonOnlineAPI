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
    @Query("SELECT tp FROM TrainingPlan tp WHERE tp.input.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<TrainingPlan>

    @Query("SELECT tp FROM TrainingPlan tp WHERE tp.input.user.id = :userId ORDER BY tp.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(@Param("userId") userId: Long): List<TrainingPlan>

    @Query("""
    SELECT tp FROM TrainingPlan tp 
    WHERE tp.input.user.id = :userId 
    AND tp.status = :status 
    AND tp.startDate <= :start AND tp.endDate >= :end
""")
    fun findByUserIdAndStatusAndStartDateBeforeAndEndDateAfter(
        @Param("userId") userId: Long,
        @Param("status") status: ETrainingPlanStatus,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): TrainingPlan?

    @Query("""
    SELECT tp FROM TrainingPlan tp
    WHERE tp.input.user.id = :userId AND tp.status = :status
""")
    fun findByUserIdAndStatus(
        @Param("userId") userId: Long,
        @Param("status") status: ETrainingPlanStatus
    ): List<TrainingPlan>


    @Query("""
    SELECT 
        tp.id AS id, 
        tp.name AS name, 
        tp.startDate AS startDate, 
        tp.endDate AS endDate, 
        tp.status AS status 
    FROM TrainingPlan tp 
    WHERE 
        tp.input.user.id = :userId AND 
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

    @Query("""
    SELECT t FROM TrainingPlan t 
    WHERE t.input.user.id = :userId 
    AND t.status = :status 
    ORDER BY t.startDate DESC
""")
    fun findTopByUserIdAndStatusOrderByStartDateDesc(
        @Param("userId") userId: Long,
        @Param("status") status: ETrainingPlanStatus
    ): TrainingPlan?

    fun findByStatusAndStartDateBeforeAndEndDateAfter(
        status: ETrainingPlanStatus,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<TrainingPlan>
}
