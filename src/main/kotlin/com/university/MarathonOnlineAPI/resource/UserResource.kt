package com.university.MarathonOnlineAPI.resource

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserResource(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(UserResource::class.java)

    @PostMapping("/add")
    fun addUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<UserDTO>{
        val addedUser = userService.addUser(userDTO)
        return ResponseEntity(addedUser, HttpStatus.CREATED)
    }
}