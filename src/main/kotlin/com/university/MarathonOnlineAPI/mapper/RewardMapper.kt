package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RewardDTO
import com.university.MarathonOnlineAPI.entity.Reward
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RewardMapper(private val modelMapper: ModelMapper): Mapper<RewardDTO, Reward> {
    override fun toDto(entity: Reward): RewardDTO {
        return modelMapper.map(entity, RewardDTO::class.java)
    }
    override fun toEntity(dto: RewardDTO): Reward {
        return modelMapper.map(dto, Reward::class.java)
    }
}