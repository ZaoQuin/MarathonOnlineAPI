package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.TrainingFeedback
import java.time.LocalDateTime

data class TrainingDayDTO(
    var id: Long? = null,
    var week: Int? = null,
    var dayOfWeek: Int? = null,
    var session: TrainingSessionDTO? = null,
    var record: RecordDTO? = null,
    var status: ETrainingDayStatus?= null,
    var dateTime: LocalDateTime?= null,
    var trainingFeedback: TrainingFeedbackDTO? = null,
    var completionPercentage: Double? = null
)