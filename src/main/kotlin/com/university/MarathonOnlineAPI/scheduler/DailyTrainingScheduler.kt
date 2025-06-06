package com.university.MarathonOnlineAPI.scheduler

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
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
    @Scheduled(cron = "0 0 0 * * *") // Chạy lúc 00:00 mỗi ngày
    @Transactional
    fun generateDailyTraining() {
        val now = LocalDateTime.now()
        val activePlans = trainingPlanRepository.findByStatusAndStartDateBeforeAndEndDateAfter(
            ETrainingPlanStatus.ACTIVE, now, now
        )

        activePlans.forEach { plan ->
            // Đánh dấu các ngày trước đó là MISSED nếu chưa hoàn thành
            markMissedTrainingDays(plan.id!!, now)

            // Tạo TrainingDay mới cho ngày hiện tại nếu chưa tồn tại
            val existingDay = trainingDayRepository.findByPlanIdAndDateTime(plan.id!!, now)
            if (existingDay == null) {
                aiTrainingPlanService.generateTrainingDayForDate(plan.input, plan, now)
            }
        }
    }

    private fun markMissedTrainingDays(planId: Long, currentDate: LocalDateTime) {
        val previousDays = trainingDayRepository.findByPlanIdAndDateTimeBefore(planId, currentDate)
        previousDays.forEach { day ->
            if (day.status == ETrainingDayStatus.ACTIVE && day.trainingFeedback == null && (day.record == null)) {
                day.status = ETrainingDayStatus.MISSED
                trainingDayRepository.save(day)
            }
        }
    }
}