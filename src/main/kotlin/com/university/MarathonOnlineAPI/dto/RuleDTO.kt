package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class RuleDTO (
    var id: Long? = null,
    var icon: String? = null,
    var name: String? = null,
    var description: String? = null,
    var updateDate: LocalDateTime? = null,
    var contestId: Long? = null
)
