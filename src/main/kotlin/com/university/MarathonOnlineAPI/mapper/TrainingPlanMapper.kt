package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingPlanMapper(private val modelMapper: ModelMapper): Mapper<TrainingPlanDTO, TrainingPlan> {
    override fun toDto(entity: TrainingPlan): TrainingPlanDTO {
        return modelMapper.map(entity, TrainingPlanDTO::class.java)
    }
    override fun toEntity(dto: TrainingPlanDTO): TrainingPlan {
        return modelMapper.map(dto, TrainingPlan::class.java)
    }
}