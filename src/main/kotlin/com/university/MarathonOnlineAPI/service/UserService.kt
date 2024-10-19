package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO

interface UserService {
    fun addUser(userDTO: UserDTO): UserDTO
    fun deleteUserById(id: Long)
    fun updateUser(userDTO: UserDTO): UserDTO
    fun getUsers(): List<UserDTO>
    fun getById(id: Long): UserDTO
}