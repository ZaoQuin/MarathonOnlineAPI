package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.request.CreateUserRequest

interface UserService {
    fun addUser(newUser: CreateUserRequest): UserDTO
    fun deleteUserById(id: Long)
    fun updateUser(userDTO: UserDTO): UserDTO
    fun getUsers(): List<UserDTO>
    fun getById(id: Long): UserDTO
}