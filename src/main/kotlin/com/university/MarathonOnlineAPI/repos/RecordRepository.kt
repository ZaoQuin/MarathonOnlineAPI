package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.ERecordApprovalStatus
import com.university.MarathonOnlineAPI.entity.Record
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RecordRepository : JpaRepository<Record, Long> {
    @EntityGraph(attributePaths = ["approval"])
    fun findByUserIdAndApprovalApprovalStatusIn(
        userId: Long,
        statuses: List<ERecordApprovalStatus>
    ): List<Record>

    @EntityGraph(attributePaths = ["approval"])
    fun findByUserIdAndApprovalApprovalStatusInOrderByStartTimeDesc(
        userId: Long,
        statuses: List<ERecordApprovalStatus>
    ): List<Record>

    @Query("""
        SELECT r FROM Record r 
        WHERE r.user.id = :runnerId 
        AND ((r.startTime <= :referenceEndTime AND r.endTime >= :referenceStartTime))
        ORDER BY r.startTime ASC
    """)

    fun findPotentialDuplicates(
        runnerId: Long,
        referenceStartTime: LocalDateTime,
        referenceEndTime: LocalDateTime
    ): List<Record>

    fun findByUserIdAndStartTimeAndEndTime(
        userId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Record>

    fun findByUserIdAndApprovalApprovalStatusInAndStartTimeBetweenOrderByStartTimeDesc(
        userId: Long,
        approvalStatus: List<ERecordApprovalStatus>,
        startTimeStart: LocalDateTime,
        startTimeEnd: LocalDateTime
    ): List<Record>
}
