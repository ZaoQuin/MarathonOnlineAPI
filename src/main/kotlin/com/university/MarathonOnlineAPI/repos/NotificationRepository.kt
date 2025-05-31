package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ENotificationType
import com.university.MarathonOnlineAPI.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.contest.id = :contestId")
    fun deleteByContestId(contestId: Long)

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId ORDER BY n.createAt DESC")
    fun getByReceiverId(@Param("receiverId") receiverId: Long): List<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun countByReceiverIdAndIsReadFalse(@Param("receiverId") receiverId: Long): Int

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false ORDER BY n.createAt DESC")
    fun findUnreadByReceiverId(@Param("receiverId") receiverId: Long): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId AND n.type = :type ORDER BY n.createAt DESC")
    fun findByReceiverIdAndType(
        @Param("receiverId") receiverId: Long,
        @Param("type") type: ENotificationType
    ): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.contest.id = :contestId ORDER BY n.createAt DESC")
    fun findByContestId(@Param("contestId") contestId: Long): List<Notification>

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun markAllAsReadByReceiverId(@Param("receiverId") receiverId: Long)

    fun deleteByReceiverId(receiverId: Long)
}
