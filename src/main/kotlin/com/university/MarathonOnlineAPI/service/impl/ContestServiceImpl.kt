package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.mapper.ContestMapper
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.service.ContestService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class ContestServiceImpl(
    private val contestRepository: ContestRepository,
    private val contestMapper: ContestMapper
) : ContestService {

    private val logger = LoggerFactory.getLogger(ContestServiceImpl::class.java)

    override fun addContest(newContest: ContestDTO): ContestDTO {
        try {
            val contest = contestMapper.toEntity(newContest)
            val savedContest = contestRepository.save(contest)
            return contestMapper.toDto(savedContest)
        } catch (e: DataAccessException) {
            throw ContestException("Database error occurred while saving contest: ${e.message}")
        }
    }

    override fun deleteContestById(id: Long) {
        try {
            contestRepository.deleteById(id)
            logger.info("Contest with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting contest with ID $id: ${e.message}")
            throw ContestException("Database error occurred while deleting contest: ${e.message}")
        }
    }

    override fun updateContest(contestDTO: ContestDTO): ContestDTO {
        return try {
            val contestEntity = contestMapper.toEntity(contestDTO)
            val updatedContest = contestRepository.save(contestEntity)
            contestMapper.toDto(updatedContest)
        } catch (e: DataAccessException) {
            logger.error("Error updating contest: ${e.message}")
            throw ContestException("Database error occurred while updating contest: ${e.message}")
        }
    }

    override fun getContests(): List<ContestDTO> {
        return try {
            val contests = contestRepository.findAll()
            contests.map { contestMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contests: ${e.message}")
            throw ContestException("Database error occurred while retrieving contests: ${e.message}")
        }
    }

    override fun getById(id: Long): ContestDTO {
        return try {
            val contest = contestRepository.findById(id)
                .orElseThrow { ContestException("Contest with ID $id not found") }
            contestMapper.toDto(contest)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving contest with ID $id: ${e.message}")
            throw ContestException("Database error occurred while retrieving contest: ${e.message}")
        }
    }
}
