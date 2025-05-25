package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.TrainingDay
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
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
          AND td.dateTime < :now
    """)
    fun markMissedTrainingDays(
        @Param("planId") planId: Long,
        @Param("now") now: LocalDateTime,
        @Param("activeStatus") activeStatus: ETrainingDayStatus = ETrainingDayStatus.ACTIVE,
        @Param("missedStatus") missedStatus: ETrainingDayStatus = ETrainingDayStatus.MISSED
    ): Int

}
