package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.contest.CreateContestRequest
import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule
import org.springframework.http.ResponseEntity

interface ContestService {
    fun addContest(createContestRequest: CreateContestRequest, jwt: String): ContestDTO
    fun deleteContestById(id: Long)
    fun updateContest(contestDTO: ContestDTO): ContestDTO
    fun getContests(): List<ContestDTO>
    fun approveContest(contestId: Long): ContestDTO
    fun getContestByJwt(jwt: String): List<ContestDTO>
    fun getById(id: Long): ContestDTO
    fun getHomeContests(): List<ContestDTO>
    fun getContestsByRunner(jwt: String): List<ContestDTO>
}