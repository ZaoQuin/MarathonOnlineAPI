package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.entity.Contest
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class ContestMapper(private val modelMapper: ModelMapper): Mapper<ContestDTO, Contest> {
    override fun toDto(entity: Contest): ContestDTO {
        return modelMapper.map(entity, ContestDTO::class.java)
    }

    override fun toEntity(dto: ContestDTO): Contest {
        return modelMapper.map(dto, Contest::class.java)
    }
}