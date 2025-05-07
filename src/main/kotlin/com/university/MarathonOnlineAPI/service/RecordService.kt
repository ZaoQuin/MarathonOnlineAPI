package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO

interface RecordService {
    fun addRace(newRace: CreateRecordRequest, jwt: String): RecordDTO
    fun deleteRaceById(id: Long)
    fun updateRace(recordDTO: RecordDTO): RecordDTO
    fun getRaces(): List<RecordDTO>
    fun getById(id: Long): RecordDTO
    abstract fun getRacesByToken(jwt: String): List<RecordDTO>
}