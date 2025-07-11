package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.RegistrationDTO

interface RegistrationService {
    fun registerForContest(contest: ContestDTO, jwt: String): RegistrationDTO
    fun deleteRegistrationById(id: Long)
    fun updateRegistration(registrationDTO: RegistrationDTO): RegistrationDTO
    fun getRegistrations(): List<RegistrationDTO>
    fun getRegistrationByJwt(jwt: String): List<RegistrationDTO>
    fun getById(id: Long): RegistrationDTO
    fun saveRecordIntoRegistration(race: RecordDTO, jwt: String): List<RegistrationDTO>
    fun getRevenueByMonth(year: Int): List<Map<String, Any>>
    fun getRevenueByWeek(year: Int): List<Map<String, Any>>
    fun getRevenueByYear(): List<Map<String, Any>>
    fun block(registrationDTO: RegistrationDTO): RegistrationDTO
    fun awardPrizes(contestDTO: ContestDTO): List<RegistrationDTO>
}