package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RaceMapper(private val modelMapper: ModelMapper, private val userMapper: UserMapper) : Mapper<RaceDTO, Race> {

    override fun toDto(entity: Race): RaceDTO {
        val raceDTO = modelMapper.map(entity, RaceDTO::class.java)
        raceDTO.user = entity.user?.let { userMapper.toDto(it) } // Ánh xạ User sang UserDTO
        return raceDTO
    }

    override fun toEntity(dto: RaceDTO): Race {
        val race = modelMapper.map(dto, Race::class.java)
        race.user = dto.user?.let { userMapper.toEntity(it) } // Ánh xạ UserDTO sang User
        return race
    }
}