package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.mapper.*
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.ContestService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ContestServiceImpl(
    private val contestRepository: ContestRepository,
    private val contestMapper: ContestMapper,
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val ruleMapper: RuleMapper,
    private val rewardMapper: RewardMapper,
    private val tokenService: TokenService,
    private val userService: UserService,
) : ContestService {

    private val logger = LoggerFactory.getLogger(ContestServiceImpl::class.java)

    override fun addContest(newContest: ContestDTO): ContestDTO {
        try {
            val contest = Contest(
                organizer = newContest.organizer?.let { userMapper.toEntity(it) },
                name = newContest.name,
                description = newContest.description,
                distance = newContest.distance,
                startDate = newContest.startDate,
                endDate = newContest.endDate,
                fee = newContest.fee,
                maxMembers = newContest.maxMembers,
                status = newContest.status,
                rules = newContest.rules?.map { ruleMapper.toEntity(it) } ?: emptyList(),
                rewards = newContest.rewards?.map { rewardMapper.toEntity(it) } ?: emptyList(),
                createDate = newContest.createDate,
                registrationDeadline = newContest.registrationDeadline
            )

            contest.rules?.forEach { it.contest = contest }
            contest.rewards?.forEach { it.contest = contest }

            val savedContest = contestRepository.save(contest)
            return contestMapper.toDto(savedContest)
        } catch (e: DataAccessException) {
            logger.error("Database error occurred while saving contest: ${e.message}", e)
            throw ContestException("Database error occurred while saving contest: ${e.message}")
        }
    }

    override fun deleteContestById(id: Long) {
        try {
            contestRepository.deleteById(id)
            logger.info("Contest with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting contest with ID $id: ${e.message}", e)
            throw ContestException("Database error occurred while deleting contest: ${e.message}")
        }
    }

    override fun updateContest(contestDTO: ContestDTO): ContestDTO {
        return try {
            // Kiểm tra xem contest có tồn tại không
            contestRepository.findById(contestDTO.id!!)
                .orElseThrow { ContestException("Contest with ID ${contestDTO.id} not found") }

            val contestEntity = contestMapper.toEntity(contestDTO)
            val updatedContest = contestRepository.save(contestEntity)
            contestMapper.toDto(updatedContest)
        } catch (e: DataAccessException) {
            logger.error("Error updating contest: ${e.message}", e)
            throw ContestException("Database error occurred while updating contest: ${e.message}")
        }
    }

    @Transactional
    override fun getContests(): List<ContestDTO> {
        return try {
            val contests = contestRepository.findAll()
            contests.map { contestMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contests: ${e.message}", e)
            throw ContestException("Database error occurred while retrieving contests: ${e.message}")
        }
    }

    override fun getContestByJwt(jwt: String): List<ContestDTO> {
        val email = tokenService.extractEmail(jwt)
            ?: throw ContestException("Email not found in the token")

        val userDTO = userService.findByEmail(email)
            ?: throw ContestException("User not found for email: $email")

        // Tìm các contest mà user là organizer hoặc đã đăng ký
        val contests = userDTO.id?.let { contestRepository.findByOrganizerOrRegistrant(it) }
            ?: throw ContestException("No registration found for user with email ${userDTO.email}")

        return contests.map { contestMapper.toDto(it) }
    }


    override fun getById(id: Long): ContestDTO {
        try {
            val savedContest = contestRepository.findById(id)
                .orElseThrow { ContestException("Contest with ID $id not found") }
            return contestMapper.toDto(savedContest)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contest with ID $id: ${e.message}", e)
            throw ContestException("Database error occurred while retrieving contest: ${e.message}")
        }
        return TODO("Provide the return value")
    }

    override fun getHomeContests(): List<ContestDTO> {
        return try {
            val contests = contestRepository.getHomeContests()
            contests.map { contestMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contests: ${e.message}", e)
            throw ContestException("Database error occurred while retrieving contests: ${e.message}")
        }
    }

    override fun getContestsByRunner(jwt: String): List<ContestDTO> {
        return try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val contests = userDTO.id?.let { contestRepository.getContestsByRunner(it) }
            contests?.map { contestMapper.toDto(it) }?: emptyList()
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contests: ${e.message}", e)
            throw ContestException("Database error occurred while retrieving contests: ${e.message}")
        }
    }
}
