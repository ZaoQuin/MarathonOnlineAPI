package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.RecordApproval
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecordApprovalRepository: JpaRepository<RecordApproval, Long> {
}