package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.ContestDTO

interface ContestService {
    fun addContest(newRule: ContestDTO): ContestDTO
    fun deleteContestById(id: Long)
    fun updateContest(contestDTO: ContestDTO): ContestDTO
    fun getContests(): List<ContestDTO>
    fun getById(id: Long): ContestDTO
}