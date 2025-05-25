package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import java.time.LocalDateTime

data class TrainingDayDTO(
    var id: Long? = null,
    var week: Int? = null,
    var dayOfWeek: Int? = null,
    var session: TrainingSessionDTO? = null,
    var records: List<RecordDTO>? = null,
    var status: ETrainingDayStatus?= null,
    var dateTime: LocalDateTime?= null
)