package com.university.MarathonOnlineAPI.controller.trainingPlan

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import com.university.MarathonOnlineAPI.service.TrainingPlanService
import com.university.MarathonOnlineAPI.view.SingleTrainingPlanView
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/training-plan")
class TrainingPlanController(
    private val trainingPlanService: TrainingPlanService
) {
    @GetMapping
    fun getCurrentTrainingPlan(@RequestHeader("Authorization") token: String): ResponseEntity<TrainingPlanDTO> {
        val jwt = token.replace("Bearer ", "")
        val plan = trainingPlanService.getTrainingPlanByJwt(jwt)
        return ResponseEntity.ok(plan)
    }

    @GetMapping("/completed")
    fun getCompletedPlans(
        @RequestHeader("Authorization") token: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDate: LocalDateTime?
    ): ResponseEntity<Page<SingleTrainingPlanView>> {
        val jwt = token.replace("Bearer ", "")
        val result = trainingPlanService.getPlansByStatus(PageRequest.of(page, size), ETrainingPlanStatus.COMPLETED, jwt, startDate, endDate)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/archived")
    fun getArchivedPlans(
        @RequestHeader("Authorization") token: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDate: LocalDateTime?
    ): ResponseEntity<Page<SingleTrainingPlanView>> {
        val jwt = token.replace("Bearer ", "")
        val result = trainingPlanService.getPlansByStatus(PageRequest.of(page, size), ETrainingPlanStatus.ARCHIVED, jwt, startDate, endDate)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/generate")
    fun generateTrainingPlan(
        @RequestHeader("Authorization") token: String,
        @RequestBody input: TrainingPlanInputDTO
    ): ResponseEntity<TrainingPlanDTO> {
        val jwt = token.replace("Bearer ", "")
        val trainingPlan = trainingPlanService.createTrainingPlan(input, jwt)
        return ResponseEntity.ok(trainingPlan)
    }

    @GetMapping("/user/{userId}")
    fun getUserTrainingPlans(
        @PathVariable userId: Long
    ): ResponseEntity<List<TrainingPlanDTO>> {
        val plans = trainingPlanService.getUserTrainingPlans(userId)
        return ResponseEntity.ok(plans)
    }

    @GetMapping("/{planId}")
    fun getTrainingPlanById(
        @PathVariable planId: Long
    ): ResponseEntity<TrainingPlanDTO> {
        val plan = trainingPlanService.getTrainingPlanById(planId)
        return ResponseEntity.ok(plan)
    }
}