package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.TrainingDay
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime


@Repository
interface TrainingDayRepository : JpaRepository<TrainingDay, Long> {
    fun findByPlanIdOrderByWeekAscDayOfWeekAsc(planId: Long): List<TrainingDay>
    @Modifying
    @Transactional
    @Query("""
        UPDATE TrainingDay td 
        SET td.status = :missedStatus 
        WHERE td.plan.id = :planId 
          AND td.status = :activeStatus 
          AND DATE(td.dateTime) < DATE(:now)
    """)
    fun markMissedTrainingDays(
        @Param("planId") planId: Long,
        @Param("now") now: LocalDateTime,
        @Param("activeStatus") activeStatus: ETrainingDayStatus = ETrainingDayStatus.ACTIVE,
        @Param("missedStatus") missedStatus: ETrainingDayStatus = ETrainingDayStatus.MISSED
    ): Int

    fun findByPlanIdAndDateTimeBefore(id: Long, date: LocalDateTime): List<TrainingDay>

    fun findByPlanIdAndDateTime(id: Long, now: LocalDateTime): TrainingDay?

    @Query("SELECT t FROM TrainingDay t WHERE t.plan.input.user.id = :userId AND t.dateTime BETWEEN :start AND :end")
    fun findByUserIdAndDateTimeRange(userId: Long, start: LocalDateTime, end: LocalDateTime): List<TrainingDay>

    @Query("SELECT td FROM TrainingDay td WHERE td.record.id = :recordId")
    fun findByRecordId(recordId: Long): List<TrainingDay>
}
