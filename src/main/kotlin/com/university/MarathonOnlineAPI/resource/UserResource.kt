package com.university.MarathonOnlineAPI.resource

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/user")
class UserResource(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(UserResource::class.java)

    @PostMapping("/add")
    fun addUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<UserDTO>{
        val addedUser = userService.addUser(userDTO)
        return ResponseEntity(addedUser, HttpStatus.CREATED)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            userService.deleteUserById(id)
            logger.info("User with ID $id deleted successfully")
            ResponseEntity.ok("User with ID $id deleted successfully")
        } catch (e: Exception) {
            logger.error("Failed to delete user with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete user with ID $id: ${e.message}")
        }
    }

    @PutMapping("/update")
    fun updateUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<UserDTO> {
        val updatedUser = userService.updateUser(userDTO)
        return ResponseEntity(updatedUser, HttpStatus.OK)
    }

    @GetMapping("/getUsers")
    fun getUsers(): ResponseEntity<List<UserDTO>> {
        return try {
            val users = userService.getUsers()
            ResponseEntity(users, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getUsers: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/get/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDTO> {
        return try {
            val foundUser = userService.getById(id)
            ResponseEntity.ok(foundUser)
        } catch (e: Exception){
            logger.error("Error in get user by Id $id")
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}