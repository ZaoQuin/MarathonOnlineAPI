package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO

interface UserService {
    fun addUser(userDTO: UserDTO): UserDTO
}