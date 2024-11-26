package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule

interface ContestService {
    fun addContest(newContest: ContestDTO): ContestDTO
    fun deleteContestById(id: Long)
    fun updateContest(contestDTO: ContestDTO): ContestDTO
    fun getContests(): List<ContestDTO>
    fun getById(id: Long): ContestDTO
}