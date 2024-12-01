package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreateRaceRequest
import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.mapper.RaceMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.RaceRepository
import com.university.MarathonOnlineAPI.service.RaceService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RaceServiceImpl @Autowired constructor(
    private val raceRepository: RaceRepository,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val raceMapper: RaceMapper,
    private val userMapper: UserMapper
) : RaceService {

    private val logger = LoggerFactory.getLogger(RaceServiceImpl::class.java)

    override fun addRace(newRace: CreateRaceRequest, jwt: String): RaceDTO {
        logger.info("Received RaceDTO: $newRace")
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val race = Race(
                steps = newRace.steps,
                distance = newRace.distance,
                timeTaken = newRace.timeTaken,
                avgSpeed = newRace.avgSpeed,
                timestamp = newRace.timestamp,
                user = userMapper.toEntity(userDTO)
            )

            logger.info("Map to Entity: $race")

            val savedRace = raceRepository.save(race)
            return raceMapper.toDto(savedRace)
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RaceException("Database error occurred while saving race: ${e.message}")
        }
    }

    override fun deleteRaceById(id: Long) {
        try {
            if (raceRepository.existsById(id)) {
                raceRepository.deleteById(id)
                logger.info("Race with ID $id deleted successfully")
            } else {
                throw RaceException("Race with ID $id not found")
            }
        } catch (e: DataAccessException) {
            logger.error("Error deleting race with ID $id: ${e.message}")
            throw RaceException("Database error occurred while deleting race: ${e.message}")
        }
    }

    override fun updateRace(raceDTO: RaceDTO): RaceDTO {
        logger.info("Updating RaceDTO: $raceDTO")
        return try {
            val race = raceMapper.toEntity(raceDTO)
            raceRepository.save(race)
            raceMapper.toDto(race)
        } catch (e: DataAccessException) {
            logger.error("Error updating race: ${e.message}")
            throw RaceException("Database error occurred while updating race: ${e.message}")
        }
    }

    override fun getRaces(): List<RaceDTO> {
        return try {
            val races = raceRepository.findAll()
            val raceDTOs = races.map { race ->
                RaceDTO(
                    id = race.id,
                    distance = race.distance,
                    timeTaken = race.timeTaken,
                    avgSpeed = race.avgSpeed,
                    timestamp = race.timestamp
                )
            }
            raceDTOs
        } catch (e: DataAccessException) {
            logger.error("Error fetching races: ${e.message}")
            throw RaceException("Database error occurred while fetching races: ${e.message}")
        }
    }

    override fun getById(id: Long): RaceDTO {
        return try {
            val race = raceRepository.findById(id)
                .orElseThrow { RaceException("Race with ID $id not found") }

            RaceDTO(
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
}
