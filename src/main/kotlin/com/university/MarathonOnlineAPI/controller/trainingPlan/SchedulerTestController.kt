package com.university.MarathonOnlineAPI.controller.trainingPlan

import com.university.MarathonOnlineAPI.scheduler.DailyTrainingScheduler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/scheduler")
class SchedulerTestController(
    private val dailyTrainingScheduler: DailyTrainingScheduler
) {

    @GetMapping("/generate-daily-training")
    fun testGenerateDailyTraining(): String {
        dailyTrainingScheduler.generateDailyTraining()
        return "Scheduler executed manually at ${java.time.LocalDateTime.now()}"
    }
}