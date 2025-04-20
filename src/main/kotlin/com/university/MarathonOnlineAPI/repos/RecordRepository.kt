package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Record
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecordRepository : JpaRepository<Record, Long> {
    fun getByUserId(id: Long): List<Record>
    fun findByUserIdOrderByTimestampDesc(userId: Long): List<Record>
}
