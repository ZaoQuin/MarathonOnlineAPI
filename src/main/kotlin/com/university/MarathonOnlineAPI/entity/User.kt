package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.util.Date

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val gender: EGender,
    val birthday: Date,
    val username: String,
    val password: String,
    val role: ERole,
    val isVerified: Boolean
)

enum class ERole {
    RUNNER, ORGANIZE, ADMIN
}

enum class EGender {
    MALE, FEMALE
}
