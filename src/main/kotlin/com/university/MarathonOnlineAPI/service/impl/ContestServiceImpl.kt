package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.mapper.*
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.ContestService
import com.university.MarathonOnlineAPI.service.RegistrationService
import com.university.MarathonOnlineAPI.service.RuleService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ContestServiceImpl(
    private val contestRepository: ContestRepository,
    private val contestMapper: ContestMapper,
    private val userRepository: UserRepository,
) : ContestService {

    private val logger = LoggerFactory.getLogger(ContestServiceImpl::class.java)

    override fun addContest(contestDTO: ContestDTO): ContestDTO {
        try {
            val user = userRepository.findById(1L).orElseThrow {
                ContestException("Organizer not found")
            }
            logger.info("User found: $user")

            // Tiếp tục với việc tạo contest
            val contest = Contest(
                id = 1L,
                organizer = User(id = 1L), //change to current user
                name = "Marathon 2024",
                description = "A great marathon event.",
                distance = 42.195,
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now(),
                fee = BigDecimal("50.00"),
                maxMembers = 100,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                rules = emptyList(),
                rewards = emptyList(),
                registrations = emptyList(),
                registrationDeadline = LocalDateTime.now()
            )

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

    override fun getContests(): List<ContestDTO> {
        return try {
            val contests = contestRepository.findAll()
            contests.map { contestMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contests: ${e.message}", e)
            throw ContestException("Database error occurred while retrieving contests: ${e.message}")
        }
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
}
