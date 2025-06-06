package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERecordSource
import java.time.LocalDateTime

data class CreateRecordRequest(
    var steps: Int? = null,
    var distance: Double? = null,
    var avgSpeed: Double? = null,
    var heartRate: Double? = null,
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var source: ERecordSource?= null,
)