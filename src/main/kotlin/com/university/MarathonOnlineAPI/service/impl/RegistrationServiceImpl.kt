package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.mapper.RegistrationMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.ContestRepository
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
    private val contestRepository: ContestRepository,
    private val recordMapper: RecordMapper,
    private val tokenService: TokenService,
    private val userMapper: UserMapper,
    private val userService: UserService
) : RegistrationService {

    private val logger = LoggerFactory.getLogger(RegistrationServiceImpl::class.java)

    override fun registerForContest(contestDTO: ContestDTO, jwt: String): RegistrationDTO {
        return try {
            val contest = contestRepository.findById(contestDTO.id!!)
                .orElseThrow { ContestException("Contest with ID ${contestDTO.id!!} not found") }
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")
            val registration = Registration(
                runner = userMapper.toEntity(userDTO),
                contest = contest,
                registrationDate = LocalDateTime.now(),
                records = emptyList(),
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

    override fun saveRecordIntoRegistration(recordDTO: RecordDTO, jwt: String): List<RegistrationDTO> {
        return try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")
            val race = recordMapper.toEntity(recordDTO)
            val registrations = userDTO.id?.let { registrationRepository.findActiveRegistration(it) }
            logger.info("saveRecordIntoRegistration", registrations)
            registrations!!.forEach { registration ->
                registration.records?.let { races ->
                    if (races is MutableList) {
                        races.add(race)
                    }
                } ?: run {
                    registration.records = mutableListOf(race)
                }

                if(registration.records?.sumOf{ it.distance!!.toDouble()}!! >= registration.contest?.distance!!)
                    registration.status = ERegistrationStatus.COMPLETED
            }

            val registrationsResult = registrationRepository.saveAll(registrations)
            registrationsResult.map { registration -> registrationMapper.toDto(registration) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getRevenueByMonth(year: Int): List<Map<String, Any>> {
        return registrationRepository.revenueByMonth(year)
    }

    override fun getRevenueByWeek(year: Int): List<Map<String, Any>> {
        return registrationRepository.revenueByWeek(year)
    }

    override fun getRevenueByYear(): List<Map<String, Any>> {
        return registrationRepository.revenueByYear()
    }

    override fun block(registrationDTO: RegistrationDTO): RegistrationDTO {
        return try {
            val registration = registrationRepository.findById(registrationDTO.id!!)
                .orElseThrow { RegistrationException("Registration with ID $registrationDTO.id not found") }
            registration.status = ERegistrationStatus.BLOCK
            registrationRepository.save(registration)
            registrationMapper.toDto(registration)
        } catch (e: DataAccessException) {
            logger.error("Error blocking registration: ${e.message}")
            throw RegistrationException("Database error occurred while blocking registration: ${e.message}")
        }
    }

    override fun awardPrizes(contestDTO: ContestDTO): List<RegistrationDTO> {
        val contest = contestRepository.findById(contestDTO.id!!)
            .orElseThrow { RegistrationException("Registration with ID $contestDTO.id not found") }
        val sortedRegistrations = contest.registrations
            ?.filter { it.status == ERegistrationStatus.COMPLETED }
            ?.sortedWith(
                compareByDescending<Registration> { reg ->
                    reg.records?.sumOf { it.distance?.toDouble() ?: 0.0 } ?: 0.0
                }.thenBy { reg ->
                    reg.records?.sumOf { it.timeTaken?.toLong() ?: 0L } ?: 0L
                }.thenBy { reg ->
                    reg.records?.map { it.avgSpeed?.toDouble() ?: 0.0 }?.average() ?: 0.0
                }.thenBy { reg ->
                    reg.registrationDate
                }
            ) ?: emptyList()
        val rewardsByRank = contest.rewards?.groupBy { it.rewardRank } ?: emptyMap()

        rewardsByRank.filterKeys { it!! > 0 }.forEach { (rank, rewards) ->
            sortedRegistrations.getOrNull(rank!! - 1)?.let { registration ->
                assignRewardsToRegistration(registration, rewards)
            }
        }

        val defaultRewards = rewardsByRank[0] ?: emptyList()
        sortedRegistrations.forEach { registration ->
            registration.contest = contest
            assignRewardsToRegistration(registration, defaultRewards)
        }


        val registrations = registrationRepository.saveAll(sortedRegistrations)
        return registrations.map { registrationMapper.toDto(it) }
    }


    private fun assignRewardsToRegistration(registration: Registration, rewards: List<Reward>) {
        rewards.forEach {
            val registrations = it.registrations!!.toMutableList()
            if(registration !in registrations)
                registrations.add(registration)
            it.registrations = registrations
        }
        registration.rewards = registration.rewards?.toMutableList()?.apply {
            val existingIds = this.map { it.id }
            addAll(rewards.filter { it.id !in existingIds })
        } ?: rewards.toMutableList()
    }
}
