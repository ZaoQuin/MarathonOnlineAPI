package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.Record
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.RecordException
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.RecordRepository
import com.university.MarathonOnlineAPI.service.RecordApprovalService
import com.university.MarathonOnlineAPI.service.RecordService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RecordServiceImpl @Autowired constructor(
    private val recordRepository: RecordRepository,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val recordMapper: RecordMapper,
    private val userMapper: UserMapper,
    private val recordApprovalService: RecordApprovalService
) : RecordService {

    private val logger = LoggerFactory.getLogger(RecordServiceImpl::class.java)

    override fun addRecord(newRecord: CreateRecordRequest, jwt: String): RecordDTO {
        logger.info("Received RecordDTO: $newRecord")
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val record = RecordDTO(
                steps = newRecord.steps,
                distance = newRecord.distance,
                timeTaken = newRecord.timeTaken,
                avgSpeed = newRecord.avgSpeed,
                timestamp = newRecord.timestamp,
                user = userDTO
            )

            logger.info("Map to Entity: $record")

            val savedRecordApproval = recordApprovalService.analyzeRecordApproval(record)

            record.approval = savedRecordApproval
            val savedRecord = recordRepository.save(recordMapper.toEntity(record))
            return recordMapper.toDto(savedRecord)
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RecordException("Database error occurred while saving race: ${e.message}")
        }
    }

    override fun deleteRecordById(id: Long) {
        try {
            if (recordRepository.existsById(id)) {
                recordRepository.deleteById(id)
                logger.info("Record with ID $id deleted successfully")
            } else {
                throw RecordException("Record with ID $id not found")
            }
        } catch (e: DataAccessException) {
            logger.error("Error deleting race with ID $id: ${e.message}")
            throw RecordException("Database error occurred while deleting race: ${e.message}")
        }
    }

    override fun updateRecord(recordDTO: RecordDTO): RecordDTO {
        logger.info("Updating RecordDTO: $recordDTO")
        return try {
            val race = recordMapper.toEntity(recordDTO)
            recordRepository.save(race)
            recordMapper.toDto(race)
        } catch (e: DataAccessException) {
            logger.error("Error updating race: ${e.message}")
            throw RecordException("Database error occurred while updating race: ${e.message}")
        }
    }

    override fun getRecords(): List<RecordDTO> {
        return try {
            val races = recordRepository.findAll()
            val raceDTOs = races.map { race ->
                recordMapper.toDto(race)
            }
            raceDTOs
        } catch (e: DataAccessException) {
            logger.error("Error fetching races: ${e.message}")
            throw RecordException("Database error occurred while fetching races: ${e.message}")
        }
    }

    override fun getById(id: Long): RecordDTO {
        return try {
            val race = recordRepository.findById(id)
                .orElseThrow { RecordException("Record with ID $id not found") }

            RecordDTO(
                id = race.id,
                distance = race.distance,
                timeTaken = race.timeTaken,
                avgSpeed = race.avgSpeed,
                timestamp = race.timestamp
            )
        } catch (e: DataAccessException) {
            logger.error("Error fetching race with ID $id: ${e.message}")
            throw RecordException("Database error occurred while fetching race: ${e.message}")
        }
    }

    override fun getRecordsByToken(jwt: String): List<RecordDTO> {
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val races = userDTO.id?.let { recordRepository.getByUserId(it) }
            return races?.map { recordMapper.toDto(it) }!!
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RecordException("Database error occurred while saving race: ${e.message}")
        }
    }
}
