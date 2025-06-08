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
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun generateDailyTraining() {
        val now = LocalDateTime.now()
        println("[Scheduler] Bắt đầu generateDailyTraining tại: $now")

        val activePlans = trainingPlanRepository
            .findByStatusAndStartDateBeforeAndEndDateAfter(ETrainingPlanStatus.ACTIVE, now, now)

        println("[Scheduler] Số lượng plan ACTIVE hợp lệ: ${activePlans.size}")

        activePlans.forEach { plan ->
            println("[Scheduler] Xử lý plan: id=${plan.id}, name=${plan.name}, start=${plan.startDate}, end=${plan.endDate}")

            val planId = plan.id
            if (planId == null) {
                println("[Scheduler][WARN] Plan ID null, bỏ qua")
                return@forEach
            }

            try {
                markMissedTrainingDays(planId, now)
            } catch (e: Exception) {
                println("[Scheduler][ERROR] Lỗi khi đánh dấu MISSED cho planId=$planId: ${e.message}")
                return@forEach
            }

            val existingDay = trainingDayRepository.findByPlanIdAndDateTime(planId, now)
            if (existingDay == null) {
                println("[Scheduler] Không tìm thấy TrainingDay hôm nay, tiến hành tạo mới")
                aiTrainingPlanService.generateTrainingDayForDate(plan.input, plan, now)
            } else {
                println("[Scheduler] Đã tồn tại TrainingDay cho hôm nay, không tạo mới")
            }
        }

        println("[Scheduler] Kết thúc generateDailyTraining")
    }

    private fun markMissedTrainingDays(planId: Long, currentDate: LocalDateTime) {
        println("[Scheduler] Đánh dấu các TrainingDay bị bỏ lỡ trước ngày: $currentDate cho planId: $planId")

        val previousDays = trainingDayRepository.findByPlanIdAndDateTimeBefore(planId, currentDate)
        println("[Scheduler] Số TrainingDay trước ngày hiện tại: ${previousDays.size}")

        previousDays.forEach { day ->
            println("[Scheduler] Kiểm tra TrainingDay: id=${day.id}, date=${day.dateTime}, status=${day.status}")
            if (day.status == ETrainingDayStatus.ACTIVE && day.trainingFeedback == null && day.record == null) {
                println("[Scheduler] Đánh dấu TrainingDay id=${day.id} là MISSED")
                day.status = ETrainingDayStatus.MISSED
                trainingDayRepository.save(day)
            }
        }
    }

}