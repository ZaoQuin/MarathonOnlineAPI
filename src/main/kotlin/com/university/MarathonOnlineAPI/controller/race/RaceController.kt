package com.university.MarathonOnlineAPI.controller.race

import com.university.MarathonOnlineAPI.dto.CreateRaceRequest
import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.service.RaceService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/race")
class RaceController(private val raceService: RaceService) {

    private val logger = LoggerFactory.getLogger(RaceController::class.java)

    @PostMapping
    fun addRace(@RequestHeader("Authorization") token: String, @RequestBody @Valid newRace: CreateRaceRequest): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val addedRace = raceService.addRace(newRace, jwt)
            logger.error("Show addedRace: $addedRace")
            ResponseEntity(addedRace, HttpStatus.CREATED)
        } catch (e: RaceException) {
            logger.error("Error adding race: ${e.message}")
            ResponseEntity("Race error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteRace(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            raceService.deleteRaceById(id)
            logger.info("Race with ID $id deleted successfully")
            ResponseEntity.ok("Race with ID $id deleted successfully")
        } catch (e: RaceException) {
            logger.error("Failed to delete race with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete race with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete race with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete race with ID $id: ${e.message}")
        }
    }

    // Cập nhật thông tin một Race
    @PutMapping
    fun updateRace(@RequestBody @Valid raceDTO: RaceDTO): ResponseEntity<RaceDTO> {
        return try {
            val updatedRace = raceService.updateRace(raceDTO)
            ResponseEntity(updatedRace, HttpStatus.OK)
        } catch (e: RaceException) {
            logger.error("Race exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RaceException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating race: ${e.message}")
            throw RaceException("Error updating race: ${e.message}")
        }
    }

    // Lấy danh sách tất cả các Race
    @GetMapping
    fun getRaces(): ResponseEntity<List<RaceDTO>> {
        return try {
            val races = raceService.getRaces()
            ResponseEntity(races, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRaces: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    // Lấy thông tin một Race theo ID
    @GetMapping("/{id}")
    fun getRaceById(@PathVariable id: Long): ResponseEntity<RaceDTO> {
        return try {
            val foundRace = raceService.getById(id)
            ResponseEntity.ok(foundRace)
        } catch (e: RaceException) {
            logger.error("Error getting race by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting race by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
