package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.ERewardType

data class RewardDTO (
    var id: Long? = null,
    var name: String? = null,
    var description: String? = null,
    var rewardRank: Int? = null,
    var type: ERewardType? = null,
    var isClaim: Boolean? = null
)