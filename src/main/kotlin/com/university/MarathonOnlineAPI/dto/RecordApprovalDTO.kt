package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERecordApprovalStatus

data class RecordApprovalDTO (
    var id: Long? = null,
    var approvalStatus: ERecordApprovalStatus? = null,
    var fraudRisk: Double? = null,           // 0-100
    var fraudType: String? = null,
    var reviewNote: String? = null
)