package com.university.MarathonOnlineAPI.controller.user

import com.university.MarathonOnlineAPI.entity.EGender
import com.university.MarathonOnlineAPI.entity.ERole
import java.time.LocalDate

data class CreateUserRequest(
    var fullName: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var gender: EGender? = null,
    var birthday: LocalDate? = null,
    var address: String? = null,
    var username: String? = null,
    var password: String? = null,
    var role: ERole? = null,
    var isVerified: Boolean = false
)