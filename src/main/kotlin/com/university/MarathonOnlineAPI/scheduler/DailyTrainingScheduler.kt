package com.university.MarathonOnlineAPI.scheduler

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.entity.ETrainingSessionType
import com.university.MarathonOnlineAPI.repos.TrainingDayRepository
import com.university.MarathonOnlineAPI.repos.TrainingPlanRepository
import com.university.MarathonOnlineAPI.service.AITrainingPlanService
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DailyTrainingScheduler(
    private val aiTrainingPlanService: AITrainingPlanService,
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingDayRepository: TrainingDayRepository
) {
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    fun markExpiredPlansAsCompleted() {
        val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()

        val expiredPlans = trainingPlanRepository.findByStatusAndEndDateBefore(
            ETrainingPlanStatus.ACTIVE,
            todayStart
        )

        expiredPlans.forEach { plan ->
            plan.status = ETrainingPlanStatus.COMPLETED
            trainingPlanRepository.save(plan)
            println("[PlanStatusScheduler] Đã chuyển TrainingPlan ${plan.id} sang COMPLETED vì quá hạn.")
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun generateDailyTraining() {
        val now = LocalDateTime.now()

        val activePlans = trainingPlanRepository
            .findByStatusAndStartDateBeforeAndEndDateAfter(ETrainingPlanStatus.ACTIVE, now, now)


        activePlans.forEach { plan ->
            val planId = plan.id
            if (planId == null) {
                return@forEach
            }

            try {
                markMissedTrainingDays(planId, now)
            } catch (e: Exception) {
                return@forEach
            }

            val existingDay = trainingDayRepository.findByPlanIdAndDateTime(planId, now)
            if (existingDay == null) {
                aiTrainingPlanService.generateTrainingDayForDate(plan.input, plan, now)
            } else {
                println("[Scheduler] Đã tồn tại TrainingDay cho hôm nay, không tạo mới")
            }
        }

    }

    private fun markMissedTrainingDays(planId: Long, currentDate: LocalDateTime) {
        val previousDays = trainingDayRepository.findByPlanIdAndDateTimeBefore(planId, currentDate)

        previousDays.forEach { day ->
            if (day.session!!.type == ETrainingSessionType.REST) {
                day.status = ETrainingDayStatus.COMPLETED
                day.completionPercentage = 100.0
                trainingDayRepository.save(day)
            } else if (day.status == ETrainingDayStatus.ACTIVE && day.trainingFeedback == null && day.record == null) {
                day.status = ETrainingDayStatus.MISSED
                trainingDayRepository.save(day)
            }
        }
    }

}