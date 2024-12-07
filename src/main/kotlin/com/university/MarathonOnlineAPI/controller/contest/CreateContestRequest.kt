package com.university.MarathonOnlineAPI.controller.contest

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.dto.RewardDTO
import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.EContestStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateContestRequest(
    var rules: List<RuleDTO>? = null,
    var rewards: List<RewardDTO>? = null,
    var name: String? = null,
    var description: String? = null,
    var distance: Double? = null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var fee: BigDecimal? = null,
    var maxMembers: Int? = null,
    var status: EContestStatus? = null,
    var registrationDeadline: LocalDateTime? = null
)
