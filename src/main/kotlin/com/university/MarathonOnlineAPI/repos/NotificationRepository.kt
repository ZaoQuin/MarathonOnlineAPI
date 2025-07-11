package com.university.MarathonOnlineAPI.repos

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
    @Query(
        """
    DELETE FROM Notification n 
    WHERE n.objectId = :objectId 
      AND n.type IN (
        com.university.MarathonOnlineAPI.entity.ENotificationType.NEW_CONTEST,
        com.university.MarathonOnlineAPI.entity.ENotificationType.BLOCK_CONTEST,
        com.university.MarathonOnlineAPI.entity.ENotificationType.ACCEPT_CONTEST,
        com.university.MarathonOnlineAPI.entity.ENotificationType.NOT_APPROVAL_CONTEST
      )
    """
    )
    fun deleteByObjectIdIfContestType(objectId: Long)

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId ORDER BY n.createAt DESC")
    fun getByReceiverId(@Param("receiverId") receiverId: Long): List<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun countByReceiverIdAndIsReadFalse(@Param("receiverId") receiverId: Long): Int
}
