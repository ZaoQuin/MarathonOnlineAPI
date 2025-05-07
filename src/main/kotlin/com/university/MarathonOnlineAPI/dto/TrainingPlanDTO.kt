package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput
import com.university.MarathonOnlineAPI.entity.TrainingSession
import com.university.MarathonOnlineAPI.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

data class TrainingPlanDTO(
    var id: Long? = null,
    var name: String? = null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var status: ETrainingPlanStatus? = null,
    var trainingDays: List<TrainingDayDTO> = emptyList()
)
