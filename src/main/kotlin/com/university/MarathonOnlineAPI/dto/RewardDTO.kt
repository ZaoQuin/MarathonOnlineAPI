package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERewardType

data class RewardDTO (
    val id: Long? = null,
    val name: String? = null,
    val desc: String? = null,
    val rank: Int? = null,
    val type: ERewardType? = null,
    val isClaim: Boolean? = null
)