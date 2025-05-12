package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO

interface RecordService {
    fun addRecord(newRace: CreateRecordRequest, jwt: String): RecordDTO
    fun deleteRecordById(id: Long)
    fun updateRecord(recordDTO: RecordDTO): RecordDTO
    fun getRecords(): List<RecordDTO>
    fun getById(id: Long): RecordDTO
    fun getRecordsByToken(jwt: String): List<RecordDTO>
}