package com.university.MarathonOnlineAPI.resource

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserResource(private val userService: UserService) {

    @PostMapping
    fun addUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<UserDTO>{
        return ResponseEntity(userService.addUser(userDTO), HttpStatus.CREATED)
    }
}