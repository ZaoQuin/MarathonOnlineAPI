package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class CreateRuleRequest (
    val icon: String? = null,
    val name: String? = null,
    val description: String? = null,
    val updateDate: LocalDateTime? = null
)
