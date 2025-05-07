package com.university.MarathonOnlineAPI.dto

data class TrainingDayDTO(
    var id: Long? = null,
    var week: Int? = null,
    var dayOfWeek: Int? = null,
    var session: TrainingSessionDTO? = null
)