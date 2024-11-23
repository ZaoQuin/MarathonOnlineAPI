package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.controller.user.CreateUserRequest
import com.university.MarathonOnlineAPI.entity.User

interface UserService {
    fun addUser(newUser: CreateUserRequest): UserDTO
    fun deleteUserById(id: Long)
    fun updateUser(userDTO: UserDTO): UserDTO
    fun getUsers(): List<UserDTO>
    fun getById(id: Long): UserDTO
    fun findByEmail(jwt: String): UserDTO
    fun removeRefreshTokenByEmail(email: String): Boolean
    fun checkEmailExists(email: String): Boolean
    fun updatePassword(email: String, password: String): Boolean

}