package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Reward
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RewardRepository : JpaRepository<Reward, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Reward r WHERE r.contest.id = :contestId")
    fun deleteByContestId(contestId: Long)
}
