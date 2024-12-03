package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRaceRequest
import com.university.MarathonOnlineAPI.dto.RaceDTO

interface RaceService {
    fun addRace(newRace: CreateRaceRequest, jwt: String): RaceDTO
    fun deleteRaceById(id: Long)
    fun updateRace(raceDTO: RaceDTO): RaceDTO
    fun getRaces(): List<RaceDTO>
    fun getById(id: Long): RaceDTO
    abstract fun getRacesByToken(jwt: String): List<RaceDTO>
}