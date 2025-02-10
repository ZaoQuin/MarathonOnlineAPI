package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.contest.id = :contestId")
    fun deleteByContestId(contestId: Long)
    fun getByReceiverId(it: Long): List<Notification>
}
