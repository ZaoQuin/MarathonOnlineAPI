package com.university.MarathonOnlineAPI.controller.contest

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.service.ContestService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/contest")
class ContestController(private val contestService: ContestService) {

    private val logger = LoggerFactory.getLogger(ContestController::class.java)

    @PostMapping
    fun addContest(@RequestBody @Valid newContest: ContestDTO): ResponseEntity<Any> {
        return try {
            val addedContest = contestService.addContest(newContest)
            ResponseEntity(addedContest, HttpStatus.CREATED)
        } catch (e: ContestException) {
            logger.error("Error adding contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteContest(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            contestService.deleteContestById(id)
            logger.info("Contest with ID $id deleted successfully")
            ResponseEntity.ok("Contest with ID $id deleted successfully")
        } catch (e: ContestException) {
            logger.error("Failed to delete contest with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete contest with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete contest with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete contest with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateContest(@RequestBody @Valid contestDTO: ContestDTO): ResponseEntity<ContestDTO> {
        return try {
            val updatedContest = contestService.updateContest(contestDTO)
            ResponseEntity(updatedContest, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Contest exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw ContestException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating contest: ${e.message}")
            throw ContestException("Error updating contest: ${e.message}")
        }
    }

    @GetMapping
    fun getContests(): ResponseEntity<*> {
        return try {
            val contests = contestService.getContests()
            when {
                contests.isEmpty() -> ResponseEntity.noContent().build()
                else -> ResponseEntity.ok(GetContestsResponse(contests))
            }
        } catch (e: ContestException) {
            logger.error("Contest retrieval error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error in getContests", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping("/jwt")
    fun getContestByJwt(@RequestHeader("Authorization") token: String): ResponseEntity<Any>{
        return try {
            val jwt = token.replace("Bearer ", "")
            val contests = contestService.getContestByJwt(jwt)
            ResponseEntity(contests, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Error contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/runner")
    fun getContestByRunner(@RequestHeader("Authorization") token: String): ResponseEntity<Any>{
        return try {
            val jwt = token.replace("Bearer ", "")
            val contests = contestService.getContestsByRunner(jwt)
            when {
                contests.isEmpty() -> ResponseEntity.noContent().build()
                else -> ResponseEntity.ok(GetContestsResponse(contests))
            }
        } catch (e: ContestException) {
            logger.error("Error contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/home")
    fun getHomeContests(): ResponseEntity<*> {
        return try {
            val contests = contestService.getHomeContests()
            when {
                contests.isEmpty() -> ResponseEntity.noContent().build()
                else -> ResponseEntity.ok(GetContestsResponse(contests))
            }
        } catch (e: ContestException) {
            logger.error("Contest retrieval error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error in getContests", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping("/{id}")
    fun getContestById(@PathVariable id: Long): ResponseEntity<ContestDTO> {
        return try {
            val foundContest = contestService.getById(id)
            ResponseEntity.ok(foundContest)
        } catch (e: ContestException) {
            logger.error("Error getting contest by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting contest by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
