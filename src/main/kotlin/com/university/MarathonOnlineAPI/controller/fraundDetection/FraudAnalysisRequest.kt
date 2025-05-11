package com.university.MarathonOnlineAPI.controller.fraundDetection

import com.university.MarathonOnlineAPI.dto.RecordDTO

data class FraudAnalysisRequest (
    var marathonData: List<RecordDTO>?= null
)