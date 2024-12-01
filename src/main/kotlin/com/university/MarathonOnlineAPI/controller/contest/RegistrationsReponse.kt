package com.university.MarathonOnlineAPI.controller.contest

import com.university.MarathonOnlineAPI.dto.RegistrationDTO

data class RegistrationsResponse (
    val registrations: List<RegistrationDTO>
)