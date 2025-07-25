package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.*
import java.time.LocalDateTime

data class RegistrationDTO (
    var id: Long? = null,
    var runner: UserDTO? = null,
    var payment: PaymentDTO? = null,
    var registrationDate: LocalDateTime? = null,
    var completedDate: LocalDateTime? = null,
    var registrationRank: Int? = null,
    var records: List<RecordDTO>? = null,
    var rewards: List<RewardDTO>? = null,
    var status: ERegistrationStatus? = null
)
