package com.university.MarathonOnlineAPI.controller.record

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.service.RecordService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/record")
class RecordController(private val recordService: RecordService) {

    private val logger = LoggerFactory.getLogger(RecordController::class.java)

    @PostMapping
    fun addRaceAndSaveIntoRegistration(@RequestHeader("Authorization") token: String, @RequestBody @Valid newRace: CreateRecordRequest): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val addedRace = recordService.addRace(newRace, jwt)
            logger.error("Show addedRace: $addedRace")
            ResponseEntity(addedRace, HttpStatus.CREATED)
        } catch (e: RaceException) {
            logger.error("Error adding record: ${e.message}")
            ResponseEntity("Race error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteRace(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            recordService.deleteRaceById(id)
            logger.info("Race with ID $id deleted successfully")
            ResponseEntity.ok("Race with ID $id deleted successfully")
        } catch (e: RaceException) {
            logger.error("Failed to delete record with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete record with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete record with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete record with ID $id: ${e.message}")
        }
    }

    // Cập nhật thông tin một Race
    @PutMapping
    fun updateRace(@RequestBody @Valid recordDTO: RecordDTO): ResponseEntity<RecordDTO> {
        return try {
            val updatedRace = recordService.updateRace(recordDTO)
            ResponseEntity(updatedRace, HttpStatus.OK)
        } catch (e: RaceException) {
            logger.error("Race exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RaceException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating record: ${e.message}")
            throw RaceException("Error updating record: ${e.message}")
        }
    }

    @GetMapping
    fun getRaces(): ResponseEntity<List<RecordDTO>> {
        return try {
            val records = recordService.getRaces()
            ResponseEntity(records, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRaces: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/runner")
    fun getByRunnerToken(@RequestHeader("Authorization") token: String): ResponseEntity<List<RecordDTO>> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val records = recordService.getRacesByToken(jwt)
            ResponseEntity(records, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRaces: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getRaceById(@PathVariable id: Long): ResponseEntity<RecordDTO> {
        return try {
            val foundRace = recordService.getById(id)
            ResponseEntity.ok(foundRace)
        } catch (e: RaceException) {
            logger.error("Error getting record by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting record by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
