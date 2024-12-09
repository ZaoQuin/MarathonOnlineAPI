package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByTokenRefresh(token: String): Optional<User>
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    override fun count(): Long
}