package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERewardType

data class CreateRewardRequest (
    val name: String? = null,
    val description: String? = null,
    val rewardRank: Int? = null,
    val type: ERewardType? = null,
    val isClaim: Boolean? = null
)