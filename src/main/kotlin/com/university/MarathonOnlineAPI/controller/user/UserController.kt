package com.university.MarathonOnlineAPI.controller.user

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping
    fun addUser(@RequestBody @Valid newUser: CreateUserRequest): ResponseEntity<Any> {
        return try {
            val addedUser = userService.addUser(newUser)
            logger.error("Show addedUser: $addedUser")
            ResponseEntity(addedUser, HttpStatus.CREATED)

        } catch (e: UserException) {
            logger.error("Error adding user: ${e.message}")
            ResponseEntity("User error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            userService.deleteUserById(id)
            logger.info("User with ID $id deleted successfully")
            ResponseEntity.ok("User with ID $id deleted successfully")
        } catch (e: UserException) {
            logger.error("Failed to delete user with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete user with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete user with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete user with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<UserDTO> {
        return try {
            val updatedUser = userService.updateUser(userDTO)
            ResponseEntity(updatedUser, HttpStatus.OK)
        } catch (e: UserException) {
            logger.error("User exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw UserException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating user: ${e.message}")
            throw UserException("Error updating user: ${e.message}")
        }

    }

    @PutMapping("/block/{id}")
    fun blockUser(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val blockedUser = userService.blockUser(id)
            ResponseEntity.ok(blockedUser)
        } catch (e: ContestException) {
            logger.error("User block error: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error blocking user: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error occurred during user block: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while blocking an user")
        }
    }

    @PutMapping("/active/{id}")
    fun unblockUser(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val unblockedUser = userService.unblockUser(id)
            ResponseEntity.ok(unblockedUser)
        } catch (e: ContestException) {
            logger.error("User approval error: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error approving contest: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error occurred during contest approval: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while approving the contest")
        }
    }

    @GetMapping
    fun getUsers(): ResponseEntity<List<UserDTO>> {
        return try {
            val users = userService.getUsers()
            ResponseEntity(users, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getUsers: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDTO> {
        return try {
            val foundUser = userService.getById(id)
            ResponseEntity.ok(foundUser)
        } catch (e: UserException) {
            logger.error("Error getting user by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting user by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/check-email")
    fun checkMail(@RequestBody request: CheckEmailRequest): ResponseEntity<CheckEmailResponse> {
        val emailExists = userService.checkEmailExists(request.email)
        return if (emailExists) {
            ResponseEntity(CheckEmailResponse(true), HttpStatus.OK)
        } else {
            ResponseEntity(CheckEmailResponse(false, "Email not found"), HttpStatus.OK)
        }
    }

    @PostMapping("/check-username")
    fun checkUsername(@RequestBody request: CheckUsernameRequest): ResponseEntity<CheckUsernameResponse> {
        val emailExists = userService.checkUsernameExists(request.username.trim())
        return if (emailExists) {
            ResponseEntity(CheckUsernameResponse(true), HttpStatus.OK)
        } else {
            ResponseEntity(CheckUsernameResponse(false, "Username not found"), HttpStatus.OK)
        }
    }

    @PostMapping("/update-password")
    fun updatePassword(@RequestBody request: UpdatePasswordRequest): ResponseEntity<UpdatePasswordResponse> {
        val updated = userService.updatePassword(request.email, request.password)
        return if (updated) {
            ResponseEntity(UpdatePasswordResponse(true), HttpStatus.OK)
        } else {
            ResponseEntity(UpdatePasswordResponse(false, "Email not found"), HttpStatus.NOT_FOUND)
        }
    }
}
