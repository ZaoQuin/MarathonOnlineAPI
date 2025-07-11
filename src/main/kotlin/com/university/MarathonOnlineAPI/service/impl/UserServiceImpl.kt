package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.User
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.controller.user.CreateUserRequest
import com.university.MarathonOnlineAPI.entity.EUserStatus
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepos: UserRepository,
    private val userMapper: UserMapper,
    private val encoder: PasswordEncoder
): UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun addUser(newUser: CreateUserRequest): UserDTO {
        logger.info("Received UserDTO: $newUser")
        try {
            newUser.password = encoder.encode(newUser.password)
            val user = User()
            user.fullName = newUser.fullName
            user.email = newUser.email
            user.phoneNumber = newUser.phoneNumber
            user.gender = newUser.gender
            user.birthday = newUser.birthday
            user.username = newUser.username
            user.address = newUser.address
            user.password = newUser.password
            user.role = newUser.role
            user.status = EUserStatus.PENDING
2
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
            val id: Long = userDTO.id ?: throw UserException("User ID cannot be null")
            val user = userRepos.findById(id)
                .orElseThrow { throw UserException("User not found with ID: $id") }

            if (userDTO.fullName.isNullOrBlank()) {
                throw UserException("Full name cannot be empty")
            }
            if (userDTO.email.isNullOrBlank()) {
                throw UserException("Email cannot be empty")
            }

            user.fullName = userDTO.fullName
            user.avatarUrl = userDTO.avatarUrl
            user.email = userDTO.email
            user.gender = userDTO.gender
            user.birthday = userDTO.birthday
            user.address = userDTO.address
            user.phoneNumber = userDTO.phoneNumber
            user.role = userDTO.role
            user.username = userDTO.username
            user.status = userDTO.status!!

            userRepos.save(user)

            return userMapper.toDto(user)
        } catch (e: UserException) {
            logger.error("Error updating user: ${e.message}", e)
            throw UserException("Error updating user: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}", e)
            throw RuntimeException("Unexpected error occurred while updating user: ${e.message}")
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

    override fun blockUser(id: Long) {
        try {
            val user = userRepos.findById(id).orElseThrow {
                throw UserException("User not found with ID: $id")
            }
            user.status = EUserStatus.DELETED
            userRepos.save(user)
            logger.info("User with ID $id blocked successfully")
        } catch (e: Exception){
            throw UserException("Error deleting user: ${e.message}")
        }
    }

    override fun unblockUser(id: Long) {
        try {
            val user = userRepos.findById(id).orElseThrow {
                throw UserException("User not found with ID: $id")
            }
            user.status = EUserStatus.PUBLIC
            userRepos.save(user)
            logger.info("User with ID $id blocked successfully")
        } catch (e: Exception){
            throw UserException("Error deleting user: ${e.message}")
        }
    }

    override fun checkUsernameExists(username: String): Boolean {
        return try {
            !userRepos.findByUsername(username).isEmpty
        } catch (e: Exception) {
            logger.error("Unexpected error checking email: ${e.message}")
            throw UserException("Unexpected error: ${e.message}")
        }
    }

    override fun checkPhoneNumberExists(phoneNumber: String): Boolean {
        return try {
            !userRepos.findByPhoneNumber(phoneNumber).isEmpty
        } catch (e: Exception) {
            logger.error("Unexpected error checking phone: ${e.message}")
            throw UserException("Unexpected error: ${e.message}")
        }
    }

    override fun updateAvatar(userId: Long, avatarUrl: String): UserDTO {
        try {
            val user = userRepos.findById(userId)
                .orElseThrow { throw UserException("User not found with ID: $userId") }

            user.avatarUrl = avatarUrl

            return userMapper.toDto(userRepos.save(user))
        } catch (e: UserException) {
            logger.error("Error updating user: ${e.message}", e)
            throw UserException("Error updating user: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}", e)
            throw RuntimeException("Unexpected error occurred while updating user: ${e.message}")
        }
    }

    override fun findByEmail(email: String): UserDTO {
        return try {
            val user = userRepos.findByEmail(email)
                .orElseThrow {
                    throw UserException("User not found with Email: $email")
                }
            userMapper.toDto(user)
        } catch (e: Exception) {
            logger.error("Error retrieving user by Email: ${e.message}")
            throw UserException("Unable to retrieve user with Email: $email")
        }
    }

    override fun removeRefreshTokenByEmail(email: String): Boolean {
        return try {
            val user = userRepos.findByEmail(email)
                .orElseThrow {
                    throw UserException("User not found with Email: $email")
                }
            user.tokenRefresh = null
            userRepos.save(user)
            true
        } catch (e: Exception) {
            logger.error("Error removing refresh token: ${e.message}")
            throw UserException("Error removing refresh token: ${e.message}")
        }
    }

    override fun checkEmailExists(email: String): Boolean {
        return try {
            !userRepos.findByEmail(email).isEmpty
        } catch (e: Exception) {
            logger.error("Unexpected error checking email: ${e.message}")
            throw UserException("Unexpected error: ${e.message}")
        }
    }

    override fun updatePassword(email: String, password: String): Boolean {
        return try {
            val user = userRepos.findByEmail(email)
                .orElseThrow {
                    throw UserException("User not found with Email: $email")
                }
            user.password = encoder.encode(password)
            userRepos.save(user)
            true
        } catch (e: Exception) {
            logger.error("Error updating password: ${e.message}")
            throw UserException("Error updating password: ${e.message}")
        }
    }
}