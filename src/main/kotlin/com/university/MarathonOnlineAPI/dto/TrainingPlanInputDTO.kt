package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingPlanInputGoal
import com.university.MarathonOnlineAPI.entity.ETrainingPlanInputLevel
import java.time.LocalDateTime

data class TrainingPlanInputDTO(
    var level: ETrainingPlanInputLevel? = null,
    var goal: ETrainingPlanInputGoal? = null,
    var maxDistance: Double? = null,
    var averagePace: Double? = null,
    var daysPerWeek: Int? = null
)