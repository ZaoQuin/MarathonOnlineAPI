package com.university.MarathonOnlineAPI.dto

import java.time.LocalDateTime

data class RuleDTO (
    val id: Long? = null,
    val icon: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val updateDate: LocalDateTime? = null
)
