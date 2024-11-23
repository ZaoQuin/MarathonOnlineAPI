package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.*
import java.time.LocalDateTime

data class RegistrationDTO (
    val id: Long? = null,
    val runner: UserDTO? = null,
    val payment: PaymentDTO? = null,
    val registrationDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val registrationRank: Int? = null,
    val raceResults: List<RaceDTO>? = null,
    val rewards: List<RewardDTO>? = null,
    val status: ERegistrationStatus? = null
)
