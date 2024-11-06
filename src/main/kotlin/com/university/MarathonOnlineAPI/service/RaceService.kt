package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RaceDTO

interface RaceService {
    fun addRace(newRule: RaceDTO): RaceDTO
    fun deleteRaceById(id: Long)
    fun updateRace(raceDTO: RaceDTO): RaceDTO
    fun getRaces(): List<RaceDTO>
    fun getById(id: Long): RaceDTO
}