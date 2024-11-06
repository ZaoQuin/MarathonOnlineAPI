package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.Registration
import com.university.MarathonOnlineAPI.exception.RegistrationException
import com.university.MarathonOnlineAPI.mapper.RegistrationMapper
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import com.university.MarathonOnlineAPI.service.RegistrationService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RegistrationServiceImpl(
    private val registrationRepository: RegistrationRepository,
    private val registrationMapper: RegistrationMapper
) : RegistrationService {

    private val logger = LoggerFactory.getLogger(RegistrationServiceImpl::class.java)

    override fun addRegistration(newRegistration: RegistrationDTO): RegistrationDTO {
        logger.info("Received RegistrationDTO: $newRegistration")
        return try {
            val registration = registrationMapper.toEntity(newRegistration)
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
}
