package com.university.MarathonOnlineAPI.controller.trainingDay

import com.university.MarathonOnlineAPI.controller.StringResponse
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.TrainingDayDTO
import com.university.MarathonOnlineAPI.exception.TrainingPlanException
import com.university.MarathonOnlineAPI.service.TrainingDayService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/training-day")
class TrainingDayController(
    private val trainingDayService: TrainingDayService
) {

    private val logger = LoggerFactory.getLogger(TrainingDayController::class.java)

    @GetMapping
    fun getCurrentTrainingDay(@RequestHeader("Authorization") token: String): ResponseEntity<TrainingDayDTO> {
        val jwt = token.replace("Bearer ", "")
        val plan = trainingDayService.getCurrentTrainingDayByJwt(jwt)
        return ResponseEntity.ok(plan)
    }

    @PostMapping("/record")
    fun saveRecordIntoTrainingDay(@RequestHeader("Authorization") token: String, @RequestBody @Valid recordDTO: RecordDTO): ResponseEntity<TrainingDayDTO> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val trainingDay = trainingDayService.saveRecordIntoTrainingDay(recordDTO, jwt)
            ResponseEntity.ok(trainingDay)
        } catch (e: TrainingPlanException) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}