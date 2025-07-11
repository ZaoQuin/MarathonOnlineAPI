package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.fraundDetection.FraudAnalysisRequest
import com.university.MarathonOnlineAPI.controller.fraundDetection.FraudAnalysisResponse

interface FraudDetectionService {
    fun analyzeMarathonData(request: FraudAnalysisRequest?): FraudAnalysisResponse;
}