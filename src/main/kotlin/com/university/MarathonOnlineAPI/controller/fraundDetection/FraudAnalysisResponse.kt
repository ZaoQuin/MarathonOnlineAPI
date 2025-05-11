package com.university.MarathonOnlineAPI.controller.fraundDetection

import com.fasterxml.jackson.annotation.JsonProperty

data class FraudAnalysisResponse(
    @JsonProperty("totalRecords")
    var totalRecords: Int? = null,

    @JsonProperty("totalFraudRecords")
    var totalFraudRecords: Int? = null,

    @JsonProperty("fraudUserIds")
    var fraudUserIds: List<String>? = null,

    @JsonProperty("userRiskScores")
    var userRiskScores: Map<String, Map<String, Any>>? = null,

    @JsonProperty("fraudRecordDetails")
    var fraudRecordDetails: List<FraudRecordDetail>? = null
)