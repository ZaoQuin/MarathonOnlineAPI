package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.mapper.RaceMapper
import com.university.MarathonOnlineAPI.repos.RaceRepository
import com.university.MarathonOnlineAPI.service.RaceService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RaceServiceImpl(
    private val raceRepository: RaceRepository,
    private val raceMapper: RaceMapper
) : RaceService {

    private val logger = LoggerFactory.getLogger(RaceServiceImpl::class.java)

    override fun addRace(newRace: RaceDTO): RaceDTO {
        logger.info("Received RaceDTO: $newRace")
        return try {
            val race = raceMapper.toEntity(newRace)
            raceRepository.save(race)
            raceMapper.toDto(race)
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
            races.map { raceMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error fetching races: ${e.message}")
            throw RaceException("Database error occurred while fetching races: ${e.message}")
        }
    }

    override fun getById(id: Long): RaceDTO {
        return try {
            val race = raceRepository.findById(id)
                .orElseThrow { RaceException("Race with ID $id not found") }
            raceMapper.toDto(race)
        } catch (e: DataAccessException) {
            logger.error("Error fetching race with ID $id: ${e.message}")
            throw RaceException("Database error occurred while fetching race: ${e.message}")
        }
    }
}
