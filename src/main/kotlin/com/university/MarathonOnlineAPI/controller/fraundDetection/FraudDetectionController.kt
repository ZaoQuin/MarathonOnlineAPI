package com.university.MarathonOnlineAPI.controller.fraundDetection

import com.university.MarathonOnlineAPI.service.FraudDetectionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/api/v1/fraud-detection")
class FraudDetectionController(private var fraudDetectionService: FraudDetectionService) {

    @PostMapping("/analyze")
    fun analyzeMarathonData(@RequestBody request: FraudAnalysisRequest?): ResponseEntity<Any> {
        return try {
            val result: FraudAnalysisResponse = fraudDetectionService.analyzeMarathonData(request)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid input")))
        } catch (e: IOException) {
            ResponseEntity.internalServerError().body(mapOf("error" to "Internal processing error: ${e.message}"))
        } catch (e: Exception) {
            // Catch tất cả lỗi còn lại, phòng hờ
            ResponseEntity.status(500).body(mapOf("error" to "Unexpected error: ${e.message}"))
        }
    }

}