package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class ContestDTO(
    val id: Long? = null,
    var organizer: UserDTO? = null,
    var rules: List<RuleDTO>? = null,
    var rewards: List<RewardDTO>? = null,
    var registrations: List<RegistrationDTO>? = null,
    val name: String? = null,
    val description: String? = null,
    val distance: Double? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val fee: BigDecimal? = null,
    val maxMembers: Int? = null,
    val status: EContestStatus? = null,
    val createDate: LocalDateTime? = null,
    val registrationDeadline: LocalDateTime? = null
)
