package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RecordApprovalDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO

interface RecordApprovalService {
    fun analyzeRecordApproval(recordDTO: RecordDTO): RecordApprovalDTO
    fun saveRecordApproval(recordDTO: RecordDTO): RecordApprovalDTO
    fun updateApprovalStatus(recordId: Long, approvalDTO: RecordApprovalDTO): RecordApprovalDTO
    fun getRecordApproval(recordId: Long): RecordApprovalDTO?
    fun getPendingRecords(): List<RecordDTO>
}