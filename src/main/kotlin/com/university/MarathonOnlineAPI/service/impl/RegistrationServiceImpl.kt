package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.entity.Registration
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.mapper.ContestMapper
import com.university.MarathonOnlineAPI.mapper.RaceMapper
import com.university.MarathonOnlineAPI.mapper.RegistrationMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import com.university.MarathonOnlineAPI.service.RegistrationService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RegistrationServiceImpl(
    private val registrationRepository: RegistrationRepository,
    private val registrationMapper: RegistrationMapper,
    private val raceMapper: RaceMapper,
    private val tokenService: TokenService,
    private val userMapper: UserMapper,
    private val userService: UserService,
    private val contestMapper: ContestMapper,
) : RegistrationService {

    private val logger = LoggerFactory.getLogger(RegistrationServiceImpl::class.java)

    override fun registerForContest(contest: ContestDTO, jwt: String): RegistrationDTO {
        return try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")
            val registration = Registration(
                runner = userMapper.toEntity(userDTO),
                contest = contestMapper.toEntity(contest),
                registrationDate = LocalDateTime.now(),
                races = emptyList(),
                status = ERegistrationStatus.PENDING
            )
            registrationRepository.save(registration)
            registrationMapper.toDto(registration)
        } catch (e: DataAccessException) {
            logger.error("Error saving registration: ${e.message}")
            throw RegistrationException("Database error occurred while saving registration: ${e.message}")
        }
    }

    override fun deleteRegistrationById(id: Long) {
        return try {
            if (registrationRepository.existsById(id)) {
                registrationRepository.deleteById(id)
                logger.info("Registration with ID $id deleted successfully")
            } else {
                throw RegistrationException("Registration with ID $id not found")
            }
        } catch (e: DataAccessException) {
            logger.error("Error deleting registration with ID $id: ${e.message}")
            throw RegistrationException("Database error occurred while deleting registration: ${e.message}")
        }
    }

    override fun updateRegistration(registrationDTO: RegistrationDTO): RegistrationDTO {
        logger.info("Updating RegistrationDTO: $registrationDTO")
        return try {
            val registration = registrationMapper.toEntity(registrationDTO)
            registrationRepository.save(registration)
            registrationMapper.toDto(registration)
        } catch (e: DataAccessException) {
            logger.error("Error updating registration: ${e.message}")
            throw RegistrationException("Database error occurred while updating registration: ${e.message}")
        }
    }

    override fun getRegistrations(): List<RegistrationDTO> {
        return try {
            val registrations = registrationRepository.findAll()
            registrations.map { registrationMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error fetching registrations: ${e.message}")
            throw RegistrationException("Database error occurred while fetching registrations: ${e.message}")
        }
    }

    override fun getById(id: Long): RegistrationDTO {
        return try {
            val registration = registrationRepository.findById(id)
                .orElseThrow { RegistrationException("Registration with ID $id not found") }
            registrationMapper.toDto(registration)
        } catch (e: DataAccessException) {
            logger.error("Error fetching registration with ID $id: ${e.message}")
            throw RegistrationException("Database error occurred while fetching registration: ${e.message}")
        }
    }

    override fun getRegistrationByJwt(jwt: String): List<RegistrationDTO> {
        val userDTO = tokenService.extractEmail(jwt)?.let { email ->
            userService.findByEmail(email)
        } ?: throw AuthenticationException("Email not found in the token")

        val registrations = userDTO.email?.let { registrationRepository.findByRunnerEmail(it) }
            ?: throw RegistrationException("No registration found for user with email ${userDTO.email}")

        return registrations.map { registrationMapper.toDto(it) }
    }


    override fun saveRaceIntoRegistration(raceDTO: RaceDTO, jwt: String): List<RegistrationDTO> {
        return try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")
            val race = raceMapper.toEntity(raceDTO)
            val registrations = userDTO.id?.let { registrationRepository.findActiveRegistration(it) }
            logger.info("saveRaceIntoRegistration", registrations)
            registrations!!.forEach { registration ->
                registration.races?.let { races ->
                    if (races is MutableList) {
                        races.add(race)
                    }
                } ?: run {
                    registration.races = mutableListOf(race)
                }

                if(registration.races?.sumOf{ it.distance!!.toDouble()}!! >= registration.contest?.distance!!)
                    registration.status = ERegistrationStatus.COMPLETED
            }

            val registrationsResult = registrationRepository.saveAll(registrations)
            registrationsResult.map { registration -> registrationMapper.toDto(registration) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
