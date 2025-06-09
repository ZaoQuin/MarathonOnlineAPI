package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository: JpaRepository<Feedback, Long> {
    @Query("SELECT f FROM Feedback f JOIN f.approval a JOIN Record r ON r.approval.id = a.id WHERE r.id = :recordId ORDER BY f.sentAt DESC")
    fun findByRecordIdOrderBySentAtDesc(@Param("recordId") recordId: Long): List<Feedback>

    // Existing methods that should work fine
    fun findByApprovalIdOrderBySentAtDesc(approvalId: Long): List<Feedback>

    fun findBySenderIdOrderBySentAtDesc(senderId: Long): List<Feedback>

    fun countByApprovalId(approvalId: Long): Long

    @Query("SELECT f FROM Feedback f WHERE f.approval.id IN :approvalIds ORDER BY f.sentAt DESC")
    fun findByApprovalIdsOrderBySentAtDesc(@Param("approvalIds") approvalIds: List<Long>): List<Feedback>
}