package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRewardRequest
import com.university.MarathonOnlineAPI.dto.RewardDTO

interface RewardService {
    fun addReward(newReward: CreateRewardRequest): RewardDTO
    fun deleteRewardById(id: Long)
    fun updateReward(rewardDTO: RewardDTO): RewardDTO
    fun getRewards(): List<RewardDTO>
    fun getById(id: Long): RewardDTO
}