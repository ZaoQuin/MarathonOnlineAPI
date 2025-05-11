package com.university.MarathonOnlineAPI.controller.fraundDetection

import com.fasterxml.jackson.annotation.JsonProperty

data class FraudRecordDetail(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("userId")
    val userId: String,

    @JsonProperty("fraudType")
    val fraudType: String,

    @JsonProperty("riskScore")
    val riskScore: Double,

    @JsonProperty("activityData")
    val activityData: Map<String, Any>
)