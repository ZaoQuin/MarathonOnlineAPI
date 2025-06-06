package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERecordSource
import java.time.LocalDateTime

data class RecordDTO(
    var id: Long? = null,
    var user: UserDTO? = null,
    var steps: Int? = null,
    var distance: Double? = null,
    var timeTaken: Long? = null,
    var avgSpeed: Double? = null,
    var heartRate: Double? = null,
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var source: ERecordSource?= null,
    var approval: RecordApprovalDTO? = null
)
