package com.university.MarathonOnlineAPI.controller.trainingPlan

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.TrainingPlanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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