package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.TrainingDay
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TrainingDayRepository : JpaRepository<TrainingDay, Long> {
    fun findByPlanIdOrderByWeekAscDayOfWeekAsc(planId: Long): List<TrainingDay>

}
