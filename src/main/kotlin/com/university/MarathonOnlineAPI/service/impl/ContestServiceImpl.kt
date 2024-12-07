package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.controller.contest.CreateContestRequest
import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.mapper.*
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.ContestService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContestServiceImpl(
    private val contestRepository: ContestRepository,
    private val contestMapper: ContestMapper,
    private val userMapper: UserMapper,
    private val ruleMapper: RuleMapper,
    private val rewardMapper: RewardMapper,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val notificationRepository: NotificationRepository,
    private val ruleRepository: RuleRepository,
    private val rewardRepository: RewardRepository
) : ContestService {

    private val logger = LoggerFactory.getLogger(ContestServiceImpl::class.java)

    override fun addContest(request: CreateContestRequest, jwt: String): ContestDTO {
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            logger.info(request.toString())

            val rules = request.rules?.map { ruleMapper.toEntity(it) } ?: emptyList()
            val rewards = request.rewards?.map { rewardMapper.toEntity(it) } ?: emptyList()

            val contest = Contest(
                organizer = userMapper.toEntity(userDTO),
                name = request.name,
                description = request.description,
                distance = request.distance,
                startDate = request.startDate,
                endDate = request.endDate,
                fee = request.fee,
                maxMembers = request.maxMembers,
                status = request.status,
                createDate = LocalDateTime.now(),
                registrationDeadline = request.registrationDeadline
            )


            val savedContest = contestRepository.save(contest)
            rules.forEach { it.contest = savedContest }
            rewards.forEach {it.contest = savedContest }
            savedContest.rules = rules
            savedContest.rewards = rewards

            val finalSavedContest  = contestRepository.save(savedContest)

            return contestMapper.toDto(finalSavedContest)
        } catch (e: DataAccessException) {
            logger.error("Database error occurred while saving contest: ${e.message}", e)
            throw ContestException("Database error occurred while saving contest: ${e.message}")
        }
    }
    override fun approveContest(id: Long): ContestDTO {
        val contest = contestRepository.findById(id)
            .orElseThrow { ContestException("Contest with ID $id not found") }

        contest.status = EContestStatus.ACTIVE

        val updatedContest = contestRepository.save(contest)

        return contestMapper.toDto(updatedContest)
    }

    override fun deleteContestById(id: Long) {
        try {
            notificationRepository.deleteByContestId(id)
            contestRepository.deleteById(id)
            logger.info("Contest with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting contest with ID $id: ${e.message}", e)
            throw ContestException("Database error occurred while deleting contest: ${e.message}")
        }
    }

    override fun updateContest(contestDTO: ContestDTO): ContestDTO {
        return try {
            val existingContest = contestRepository.findById(contestDTO.id!!)
                .orElseThrow { ContestException("Contest with ID ${contestDTO.id} not found") }

            val contestEntity = contestMapper.toEntity(contestDTO)

            val existingRules = existingContest.rules ?: emptyList()
            val existingRewards = existingContest.rewards ?: emptyList()

            val newRuleIds = contestEntity.rules?.map { it.id } ?: emptyList()
            val newRewardIds = contestEntity.rewards?.map { it.id } ?: emptyList()

            val rulesToDelete = existingRules.filter { it.id !in newRuleIds }
            val rewardsToDelete = existingRewards.filter { it.id !in newRewardIds }

            rulesToDelete.forEach { ruleRepository.delete(it) }
            rewardsToDelete.forEach { rewardRepository.delete(it) }

            contestEntity.rules?.forEach { rule ->
                rule.contest = contestEntity
                ruleRepository.save(rule)
            }

            contestEntity.rewards?.forEach { reward ->
                reward.contest = contestEntity
                rewardRepository.save(reward)
            }


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
