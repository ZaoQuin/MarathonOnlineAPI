package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.RunningStatsDTO
import java.time.LocalDateTime

interface RecordService {
    fun addRecord(newRace: CreateRecordRequest, jwt: String): RecordDTO
    fun deleteRecordById(id: Long)
    fun updateRecord(recordDTO: RecordDTO): RecordDTO
    fun getRecords(): List<RecordDTO>
    fun getById(id: Long): RecordDTO
    fun getRecordsByToken(jwt: String): List<RecordDTO>
    fun getRunningStatsByUser(userId: Long): RunningStatsDTO?
    fun getRecordsByUserId(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<RecordDTO>
    fun sync(recordDTOs: List<CreateRecordRequest>, jwt: String): List<RecordDTO>
}