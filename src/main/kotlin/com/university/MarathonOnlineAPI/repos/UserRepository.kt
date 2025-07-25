package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ERole
import com.university.MarathonOnlineAPI.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findAllByRole(role: ERole): List<User>
    fun findByTokenRefresh(token: String): Optional<User>
    @Query("SELECT u FROM User u WHERE u.role = :role")
    fun findByRole(@Param("role") role: ERole): List<User>
    @Query("SELECT u FROM User u WHERE u.username = :username")
    fun findByUsername(username: String): Optional<User>
    fun findUserById(id: Long): Optional<User>
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    fun findByPhoneNumber(phoneNumber: String): Optional<User>
}