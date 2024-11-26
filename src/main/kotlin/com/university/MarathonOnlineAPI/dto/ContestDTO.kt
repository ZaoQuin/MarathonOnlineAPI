package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class ContestDTO(
    var id: Long? = null,
    var organizer: UserDTO? = null,
    var rules: List<RuleDTO>? = null,
    var rewards: List<RewardDTO>? = null,
    var registrations: List<RegistrationDTO>? = null,
    var name: String? = null,
    var description: String? = null,
    var distance: Double? = null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var fee: BigDecimal? = null,
    var maxMembers: Int? = null,
    var status: EContestStatus? = null,
    var createDate: LocalDateTime? = null,
    var registrationDeadline: LocalDateTime? = null
)
