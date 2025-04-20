package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingPlanInputMapper  (private val modelMapper: ModelMapper): Mapper<TrainingPlanInputDTO, TrainingPlanInput> {
    override fun toDto(entity: TrainingPlanInput): TrainingPlanInputDTO {
        val trainingPlanInputDTO = modelMapper.map(entity, TrainingPlanInputDTO::class.java)
        return trainingPlanInputDTO
    }
    override fun toEntity(dto: TrainingPlanInputDTO): TrainingPlanInput {
        val trainingPlanInput = modelMapper.map(dto, TrainingPlanInput::class.java)
        return trainingPlanInput
    }
}