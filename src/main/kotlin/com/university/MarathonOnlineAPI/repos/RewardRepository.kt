package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Reward
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RewardRepository : JpaRepository<Reward, Long>
