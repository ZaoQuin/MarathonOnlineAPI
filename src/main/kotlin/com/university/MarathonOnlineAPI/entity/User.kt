package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var fullName: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var gender: EGender? = null,
    var birthday: LocalDate? = null,
    var address: String? = null,
    var username: String? = null,
    var password: String? = null,
    var role: ERole? = null,
    var isVerified: Boolean = false,
    var tokenRefresh: String? = null
)

enum class ERole {
    RUNNER, ORGANIZE, ADMIN
}

enum class EGender {
    MALE, FEMALE
}
