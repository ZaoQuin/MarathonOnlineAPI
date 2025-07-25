package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingPlanInputGoal
import com.university.MarathonOnlineAPI.entity.ETrainingPlanInputLevel

data class TrainingPlanInputDTO(
    var level: ETrainingPlanInputLevel? = null,
    var goal: ETrainingPlanInputGoal? = null,
    var trainingWeeks: Int? = null,
    var maxDistance: Double? = null,
    var averagePace: Double? = null,
)