package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RuleRepository : JpaRepository<Rule, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Rule r WHERE r.contest.id = :contestId")
    fun deleteByContestId(contestId: Long)
}
