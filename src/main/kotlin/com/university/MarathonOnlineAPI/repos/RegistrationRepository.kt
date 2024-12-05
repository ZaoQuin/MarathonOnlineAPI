package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.EContestStatus
import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.entity.Registration
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
            "AND r.status = :activeRgStatus " +
            "AND r.contest.status = :activeCtStatus " +
            "AND r.contest.startDate <= CURRENT_DATE ")
    fun findActiveRegistration(@Param("userId") userId: Long,  @Param("activeRgStatus") activeRgStatus: ERegistrationStatus = ERegistrationStatus.ACTIVE,
                               @Param("activeCtStatus") activeCtStatus: EContestStatus = EContestStatus.ACTIVE): List<Registration>

    @Query("SELECT r FROM Registration r WHERE r.runner.email = :email")
    fun findByRunnerEmail(@Param("email") email: String): List<Registration>
}
