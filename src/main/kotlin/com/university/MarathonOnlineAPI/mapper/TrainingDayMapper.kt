package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingDayDTO
import com.university.MarathonOnlineAPI.entity.TrainingDay
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingDayMapper (private val modelMapper: ModelMapper): Mapper<TrainingDayDTO, TrainingDay> {
    override fun toDto(entity: TrainingDay): TrainingDayDTO {
        return modelMapper.map(entity, TrainingDayDTO::class.java)
    }
    override fun toEntity(dto: TrainingDayDTO): TrainingDay {
        return modelMapper.map(dto, TrainingDay::class.java)
    }
}