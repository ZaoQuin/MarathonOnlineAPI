package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EGender
import com.university.MarathonOnlineAPI.entity.ERole
import java.util.*

data class UserDTO(
    val id: Long = -1,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val gender: EGender,
    val birthday: Date = Date(),
    val username: String,
    val password: String,
    val role: ERole,
    val isVerified: Boolean = false
)
