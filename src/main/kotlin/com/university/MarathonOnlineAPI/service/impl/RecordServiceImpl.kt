package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.Record
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.RecordRepository
import com.university.MarathonOnlineAPI.service.RecordService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
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
    private val userMapper: UserMapper
) : RecordService {

    private val logger = LoggerFactory.getLogger(RecordServiceImpl::class.java)

    override fun addRace(newRace: CreateRecordRequest, jwt: String): RecordDTO {
        logger.info("Received RaceDTO: $newRace")
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val record = Record(
                steps = newRace.steps,
                distance = newRace.distance,
                timeTaken = newRace.timeTaken,
                avgSpeed = newRace.avgSpeed,
                timestamp = newRace.timestamp,
                user = userMapper.toEntity(userDTO)
            )

            logger.info("Map to Entity: $record")

            val savedRace = recordRepository.save(record)
            return recordMapper.toDto(savedRace)
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RaceException("Database error occurred while saving race: ${e.message}")
        }
    }

    override fun deleteRaceById(id: Long) {
        try {
            if (recordRepository.existsById(id)) {
                recordRepository.deleteById(id)
                logger.info("Race with ID $id deleted successfully")
            } else {
                throw RaceException("Race with ID $id not found")
            }
        } catch (e: DataAccessException) {
            logger.error("Error deleting race with ID $id: ${e.message}")
            throw RaceException("Database error occurred while deleting race: ${e.message}")
        }
    }

    override fun updateRace(recordDTO: RecordDTO): RecordDTO {
        logger.info("Updating RaceDTO: $recordDTO")
        return try {
            val race = recordMapper.toEntity(recordDTO)
            recordRepository.save(race)
            recordMapper.toDto(race)
        } catch (e: DataAccessException) {
            logger.error("Error updating race: ${e.message}")
            throw RaceException("Database error occurred while updating race: ${e.message}")
        }
    }

    override fun getRaces(): List<RecordDTO> {
        return try {
            val races = recordRepository.findAll()
            val raceDTOs = races.map { race ->
                recordMapper.toDto(race)
            }
            raceDTOs
        } catch (e: DataAccessException) {
            logger.error("Error fetching races: ${e.message}")
            throw RaceException("Database error occurred while fetching races: ${e.message}")
        }
    }

    override fun getById(id: Long): RecordDTO {
        return try {
            val race = recordRepository.findById(id)
                .orElseThrow { RaceException("Race with ID $id not found") }

            RecordDTO(
                id = race.id,
                distance = race.distance,
                timeTaken = race.timeTaken,
                avgSpeed = race.avgSpeed,
                timestamp = race.timestamp
            )
        } catch (e: DataAccessException) {
            logger.error("Error fetching race with ID $id: ${e.message}")
            throw RaceException("Database error occurred while fetching race: ${e.message}")
        }
    }

    override fun getRacesByToken(jwt: String): List<RecordDTO> {
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val races = userDTO.id?.let { recordRepository.getByUserId(it) }
            return races?.map { recordMapper.toDto(it) }!!
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RaceException("Database error occurred while saving race: ${e.message}")
        }
    }
}
