package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.EContestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ContestRepository : JpaRepository<Contest, Long> {
    fun findByName(name: String): Optional<Contest>
    @Query("""
        SELECT c FROM Contest c
        WHERE c.status = :activeStatus
          AND c.registrationDeadline > CURRENT_DATE
        ORDER BY 
          c.startDate ASC,   
          SIZE(c.registrations) DESC, 
          c.fee ASC          
    """)
    fun getHomeContests(
        @Param("activeStatus") activeStatus: EContestStatus = EContestStatus.ACTIVE
    ): List<Contest>

    @Query("""
        SELECT c FROM Contest c 
        LEFT JOIN c.registrations r 
        WHERE c.organizer.id = :userId OR r.runner.id = :userId
    """)
    fun findByOrganizerOrRegistrant(@Param("userId") userId: Long): List<Contest>

    @Query("""
        SELECT c FROM Contest c
        LEFT JOIN c.registrations r 
        WHERE r.runner.id = :userId
    """)
    fun getContestsByRunner(@Param("userId") userId: Long): List<Contest>
    fun findAllByEndDateBeforeAndStatus(now: LocalDateTime?, active: EContestStatus): List<Contest>

    @Query("SELECT COALESCE(SUM(c.fee * SIZE(c.registrations)), 0) FROM Contest c WHERE c.status = 1")
    fun sumRevenueBasedOnRegistrations(): Long
    @Query("""
        SELECT c FROM Contest c
        WHERE c.status = :activeStatus
          OR c.status = :finishedStatus
          OR c.status = :completedStatus
        ORDER BY 
          c.startDate ASC,   
          SIZE(c.registrations) DESC, 
          c.fee ASC          
    """)
    fun getActiveAndFinishedAndCompleted(
        @Param("activeStatus") activeStatus: EContestStatus = EContestStatus.ACTIVE,
        @Param("finishedStatus") finishedStatus: EContestStatus = EContestStatus.FINISHED,
        @Param("completedStatus") completedStatus: EContestStatus = EContestStatus.COMPLETED
    ): List<Contest>

    @Query("""
        SELECT c FROM Contest c 
        WHERE c.organizer.email = :email
        AND (c.status = :activeStatus OR c.status = :pendingStatus)
    """)
    fun findActiveAndPendingByEmail(@Param("email") email: String,
                                    @Param("pendingStatus") pendingStatus: EContestStatus = EContestStatus.PENDING,
                                    @Param("activeStatus") activeStatus: EContestStatus = EContestStatus.ACTIVE): Optional<List<Contest>>
}
