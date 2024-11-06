package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RegistrationDTO

interface RegistrationService {
    fun addRegistration(newRule: RegistrationDTO): RegistrationDTO
    fun deleteRegistrationById(id: Long)
    fun updateRegistration(registrationDTO: RegistrationDTO): RegistrationDTO
    fun getRegistrations(): List<RegistrationDTO>
    fun getById(id: Long): RegistrationDTO
}