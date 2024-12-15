package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.contest.CreateContestRequest
import com.university.MarathonOnlineAPI.dto.ContestDTO

interface ContestService {
    fun addContest(createContestRequest: CreateContestRequest, jwt: String): ContestDTO
    fun deleteContestById(id: Long)
    fun updateContest(contestDTO: ContestDTO): ContestDTO
    fun getContests(): List<ContestDTO>
    fun approveContest(contestId: Long): ContestDTO
    fun rejectContest(contestId: Long): ContestDTO
    fun getContestByJwt(jwt: String): List<ContestDTO>
    fun getById(id: Long): ContestDTO
    fun getHomeContests(): List<ContestDTO>
    fun getContestsByRunner(jwt: String): List<ContestDTO>
    fun getActiveAndFinished(): List<ContestDTO>
    fun cancelContest(contestDTO: ContestDTO): ContestDTO
    fun awardPrizes(contestDTO: ContestDTO): ContestDTO
    fun checkNameExist(name: String): Boolean
    fun checkActiveContest(jwt: String): Boolean
}