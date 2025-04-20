package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingSessionType
import jakarta.persistence.*

data class TrainingSessionDTO(
    var id: Long? = null,
    var name: String? = null,
    var type: ETrainingSessionType? = null,
    var distance: Double? = null,
    var pace: Double? = null,
    var notes: String? = null
)