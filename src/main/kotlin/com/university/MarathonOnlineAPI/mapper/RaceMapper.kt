package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RaceMapper(private val modelMapper: ModelMapper, private val userMapper: UserMapper) : Mapper<RaceDTO, Race> {

    override fun toDto(entity: Race): RaceDTO {
        return modelMapper.map(entity, RaceDTO::class.java)
    }

    override fun toEntity(dto: RaceDTO): Race {
        return modelMapper.map(dto, Race::class.java)
    }
}