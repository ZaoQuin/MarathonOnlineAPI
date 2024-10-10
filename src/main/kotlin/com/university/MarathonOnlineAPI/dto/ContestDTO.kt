package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class ContestDTO(
    val id: Long? = null,
    val organizer: UserDTO? = null,
    val name: String? = null,
    val desc: String? = null,
    val distance: Double? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val fee: BigDecimal? = null,
    val maxMembers: Int? = null,
    val status: EContestStatus? = null,
    val createDate: LocalDateTime? = null,
    val rules: List<RuleDTO>? = null,
    val rewards: List<RewardDTO>? = null,
    val registrations: List<RegistrationDTO>? = null,
    val registrationDeadline: LocalDateTime? = null
)