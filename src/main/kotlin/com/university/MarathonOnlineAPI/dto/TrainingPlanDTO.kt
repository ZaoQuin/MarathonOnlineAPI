package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import java.time.LocalDateTime

data class TrainingPlanDTO(
    var id: Long? = null,
    var name: String? = null,
    var input: TrainingPlanInputDTO?= null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var status: ETrainingPlanStatus? = null,
    var trainingDays: List<TrainingDayDTO> = emptyList(),
    var completedDays: Int = 0,
    var remainingDays: Int = 0,
    var totalDistance: Double = 0.0,
    var progress: Double = 0.0
)
