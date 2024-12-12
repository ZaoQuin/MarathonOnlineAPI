package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.EContestStatus
import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.entity.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RegistrationRepository : JpaRepository<Registration, Long>{
    fun findByContest(contest: Contest): List<Registration>
    @Query("SELECT r FROM Registration r " +
            "WHERE r.runner.id = :userId " +
            "AND r.status != :pendingRgStatus " +
            "AND r.status != :blockRgStatus " +
            "AND r.contest.status = :activeCtStatus " +
            "AND r.contest.startDate <= CURRENT_DATE ")
    fun findActiveRegistration(@Param("userId") userId: Long,
                               @Param("pendingRgStatus") pendingRgStatus: ERegistrationStatus = ERegistrationStatus.PENDING,
                               @Param("blockRgStatus") blockRgStatus: ERegistrationStatus = ERegistrationStatus.BLOCK,
                               @Param("activeCtStatus") activeCtStatus: EContestStatus = EContestStatus.ACTIVE): List<Registration>

    @Query("SELECT r FROM Registration r WHERE r.runner.email = :email")
    fun findByRunnerEmail(@Param("email") email: String): List<Registration>

    @Query(
        """
        SELECT MONTH(r.registrationDate) AS month, SUM(p.amount) AS revenue
        FROM Registration r
        JOIN r.payment p
        WHERE YEAR(r.registrationDate) = :year
        GROUP BY MONTH(r.registrationDate)
        ORDER BY MONTH(r.registrationDate)
        """
    )
    fun revenueByMonth(@Param("year") year: Int): List<Map<String, Any>>

    @Query(
        """
        SELECT WEEK(r.registrationDate) AS week, SUM(p.amount) AS revenue
        FROM Registration r
        JOIN r.payment p
        WHERE YEAR(r.registrationDate) = :year
        GROUP BY WEEK(r.registrationDate)
        ORDER BY WEEK(r.registrationDate)
        """
    )
    fun revenueByWeek(@Param("year") year: Int): List<Map<String, Any>>

    @Query("""
        SELECT YEAR(r.registrationDate) AS year, SUM(p.amount) AS totalRevenue
        FROM Registration r
        JOIN r.payment p
        WHERE r.status = 1
        GROUP BY YEAR(r.registrationDate)
        ORDER BY YEAR(r.registrationDate)
    """)
    fun revenueByYear(): List<Map<String, Any>>
}
