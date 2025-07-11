package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.FCMToken
import com.university.MarathonOnlineAPI.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


interface FCMTokenRepository : JpaRepository<FCMToken, Long> {
    fun findByUser(user: User): List<FCMToken>
    fun findByToken(token: String): FCMToken?
    fun findByUserIdAndDeviceId(userId: Long, deviceId: String): FCMToken?
    fun existsByToken(token: String): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM FCMToken f WHERE f.token IN :tokens")
    fun deleteByTokenIn(tokens: List<String>)

    @Modifying
    @Transactional
    fun deleteByUser(user: User)
    fun findByUserId(receiverId: Long): List<FCMToken>
}