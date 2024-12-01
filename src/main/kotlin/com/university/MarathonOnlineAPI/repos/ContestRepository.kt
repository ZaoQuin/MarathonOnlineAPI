package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.EContestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContestRepository : JpaRepository<Contest, Long> {
    fun findByName(name: String): Optional<Contest>
    @Query("""
        SELECT c FROM Contest c
        WHERE c.status IN (:activeStatus, :pendingStatus)
          AND c.registrationDeadline >= CURRENT_DATE
        ORDER BY 
          CASE c.status
            WHEN :activeStatus THEN 1
            WHEN :pendingStatus THEN 2
            ELSE 3
          END ASC,  
          c.startDate ASC,   
          SIZE(c.registrations) DESC, 
          c.fee ASC          
    """)
    fun getHomeContests(
        @Param("activeStatus") activeStatus: EContestStatus = EContestStatus.ACTIVE,
        @Param("pendingStatus") pendingStatus: EContestStatus = EContestStatus.PENDING
    ): List<Contest>
}
