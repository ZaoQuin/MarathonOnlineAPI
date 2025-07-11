package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.controller.user.CreateUserRequest

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
    fun blockUser(id: Long)
    fun unblockUser(id: Long)
    fun checkUsernameExists(username: String): Boolean
    fun checkPhoneNumberExists(phoneNumber: String): Boolean
    fun updateAvatar(userId: Long, avatarUrl: String): UserDTO
}