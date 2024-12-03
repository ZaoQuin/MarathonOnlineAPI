package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.Registration
import com.university.MarathonOnlineAPI.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RegistrationRepository : JpaRepository<Registration, Long>{
    fun findByContest(contest: Contest): List<Registration>
    @Query("SELECT r FROM Registration r " +
            "WHERE r.runner.id = :userId " +
            "AND r.registrationDate <= :currentTime " +
            "AND r.status = 0 " +
            "AND r.contest.startDate <= :currentTime " +
            "AND r.contest.endDate >= :currentTime")
    fun findActiveRegistration(@Param("currentTime") currentTime: LocalDateTime,
                               @Param("userId") userId: Long): List<Registration>

    @Query("SELECT r FROM Registration r WHERE r.runner.email = :email")
    fun findByRunnerEmail(@Param("email") email: String): List<Registration>
}
