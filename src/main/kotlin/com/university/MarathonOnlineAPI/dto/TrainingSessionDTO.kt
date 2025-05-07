package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingSessionType
import java.time.LocalDateTime

data class TrainingSessionDTO(
    var id: Long? = null,
    var name: String? = null,
    var type: ETrainingSessionType? = null,
    var distance: Double? = null,
    var pace: Double? = null,
    var notes: String? = null,
    var dateTime: LocalDateTime?= null
)