package com.university.MarathonOnlineAPI.controller.statistics

import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val userRepository: UserRepository,
    private val contestRepository: ContestRepository
) {
    @GetMapping("/overview")
    fun getOverviewStatistics(): ResponseEntity<Map<String, Any>> {
        val totalUsers = userRepository.count()
        val totalContests = contestRepository.count()
        val totalRevenue = contestRepository.sumRevenueBasedOnRegistrations()

        val response = mapOf(
            "totalUsers" to totalUsers,
            "totalContests" to totalContests,
            "totalRevenue" to totalRevenue
        )

        return ResponseEntity.ok(response)
    }
}
