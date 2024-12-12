package com.university.MarathonOnlineAPI.controller.registration

import com.university.MarathonOnlineAPI.controller.contest.RegistrationsResponse
import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.service.RegistrationService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/registration")
class RegistrationController(private val registrationService: RegistrationService) {

    private val logger = LoggerFactory.getLogger(RegistrationController::class.java)

    @PostMapping
    fun registerForContest(@RequestHeader("Authorization") token: String, @RequestBody @Valid contestDTO: ContestDTO): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val newRegistration = registrationService.registerForContest(contestDTO, jwt)
            ResponseEntity(newRegistration, HttpStatus.CREATED)
        } catch (e: RegistrationException) {
            logger.error("Error registering for contest: ${e.message}")
            ResponseEntity("Registration error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun cancelRegistration(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            registrationService.deleteRegistrationById(id)
            logger.info("Registration with ID $id cancelled successfully")
            ResponseEntity.ok("Registration with ID $id cancelled successfully")
        } catch (e: RegistrationException) {
            logger.error("Failed to cancel registration with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to cancel registration with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to cancel registration with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to cancel registration with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateRegistration(@RequestBody @Valid registrationDTO: RegistrationDTO): ResponseEntity<RegistrationDTO> {
        return try {
            val updatedRegistration = registrationService.updateRegistration(registrationDTO)
            ResponseEntity(updatedRegistration, HttpStatus.OK)
        } catch (e: RegistrationException) {
            logger.error("Registration exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RegistrationException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating registration: ${e.message}")
            throw RegistrationException("Error updating registration: ${e.message}")
        }
    }

    @PutMapping("/block")
    fun blockRegistration(@RequestBody @Valid registrationDTO: RegistrationDTO): ResponseEntity<RegistrationDTO> {
        return try {
            val blockedRegistration = registrationService.block(registrationDTO)
            ResponseEntity(blockedRegistration, HttpStatus.OK)
        } catch (e: RegistrationException) {
            logger.error("Registration exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RegistrationException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating registration: ${e.message}")
            throw RegistrationException("Error updating registration: ${e.message}")
        }
    }

//    @GetMapping
//    fun getRegistrations(): ResponseEntity<List<RegistrationDTO>> {
//        return try {
//            val registrations = registrationService.getRegistrations()
//            ResponseEntity(registrations, HttpStatus.OK)
//        } catch (e: Exception) {
//            logger.error("Error in getRegistrations: ${e.message}")
//            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
//        }
//    }

    @GetMapping
    fun getRegistrationByJwt(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val registrations = registrationService.getRegistrationByJwt(jwt)
            ResponseEntity(registrations, HttpStatus.OK)
        } catch (e: RegistrationException) {
            logger.error("Error registering for contest: ${e.message}")
            ResponseEntity("Registration error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/{id}")
    fun getRegistrationById(@PathVariable id: Long): ResponseEntity<RegistrationDTO> {
        return try {
            val foundRegistration = registrationService.getById(id)
            ResponseEntity.ok(foundRegistration)
        } catch (e: RegistrationException) {
            logger.error("Error getting registration by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting registration by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/race")
    fun saveRaceIntoRegistration(@RequestHeader("Authorization") token: String, @RequestBody @Valid race: RaceDTO): ResponseEntity<RegistrationsResponse> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val registrations = registrationService.saveRaceIntoRegistration(race, jwt)
            ResponseEntity.ok(RegistrationsResponse(registrations))
        } catch (e: RegistrationException) {
            logger.error("Error getting registration by Race ${race.id}: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting registration by ID ${race.id}: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/by-month")
    fun getRevenueByMonth(@RequestParam year: Int): List<Map<String, Any>> {
        return registrationService.getRevenueByMonth(year)
    }

    @GetMapping("/by-week")
    fun getRevenueByWeek(@RequestParam year: Int): List<Map<String, Any>> {
        return registrationService.getRevenueByWeek(year)
    }

    @GetMapping("/by-year")
    fun getRevenueByYear(): List<Map<String, Any>> {
        return registrationService.getRevenueByYear()
    }
}