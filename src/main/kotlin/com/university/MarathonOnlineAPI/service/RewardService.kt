package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RewardDTO

interface RewardService {
    fun addReward(newReward: RewardDTO): RewardDTO
    fun deleteRewardById(id: Long)
    fun updateReward(rewardDTO: RewardDTO): RewardDTO
    fun getRewards(): List<RewardDTO>
    fun getById(id: Long): RewardDTO
}