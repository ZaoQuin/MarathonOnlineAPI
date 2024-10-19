package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepos: UserRepository,
    private val userMapper: UserMapper
): UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun addUser(userDTO: UserDTO): UserDTO {
        logger.info("Received UserDTO: $userDTO")
        try {
            val user = userMapper.toEntity(userDTO)
            logger.info("Mapper to Entity: $user")
            userRepos.save(user)
            return userMapper.toDto(user)
        } catch (e: Exception){
            throw UserException("Error adding user: ${e.message}")
        }
    }

    override fun deleteUserById(id: Long) {
        logger.info("Attempting to delete user with ID: $id")
        try {
            val user = userRepos.findById(id).orElseThrow {
                throw UserException("User not found with ID: $id")
            }
            userRepos.delete(user)
            logger.info("User with ID $id deleted successfully")
        } catch (e: Exception){
            throw UserException("Error deleting user: ${e.message}")
        }
    }

    override fun updateUser(userDTO: UserDTO): UserDTO {
        logger.info("Received UserDTO: $userDTO")
        try {
            val id: Long = userDTO.id?: throw UserException("User ID cannot be null");
            val user = userRepos.findById(id)
                .orElseThrow {
                    throw UserException("User not found with ID: $id")
                }

            user.fullName = userDTO.fullName
            user.email = userDTO.email
            user.gender = userDTO.gender
            user.birthday = userDTO.birthday
            user.isVerified = userDTO.isVerified
            user.phoneNumber = userDTO.phoneNumber
            user.role = userDTO.role
            user.username = userDTO.username

            userRepos.save(user)

            return userMapper.toDto(user)
        } catch (e: Exception) {
            logger.error("Error updating user: ${e.message}")
            throw UserException("Error updating user: ${e.message}")
        }
    }

    override fun getUsers(): List<UserDTO> {
        return try {
            val users = userRepos.findAll()
            users.map { userMapper.toDto(it) }
        } catch (e: Exception) {
            logger.error("Error retrieving users ${e.message}")
            throw UserException("Unable retrieving users")
        }
    }

    override fun getById(id: Long): UserDTO {
        return try {
            val user = userRepos.findById(id)
                .orElseThrow {
                    throw UserException("User not found with ID: $id")
                }
            userMapper.toDto(user)
        } catch (e: Exception) {
            logger.error("Error retrieving user by ID: ${e.message}")
            throw UserException("Unable to retrieve user with ID: $id")
        }
    }

}