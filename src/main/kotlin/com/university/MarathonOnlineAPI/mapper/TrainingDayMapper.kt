package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingDayDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.TrainingDay
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingDayMapper (private val modelMapper: ModelMapper,
                         private val trainingSessionMapper: TrainingSessionMapper): Mapper<TrainingDayDTO, TrainingDay> {
    override fun toDto(entity: TrainingDay): TrainingDayDTO {
        val dto = modelMapper.map(entity, TrainingDayDTO::class.java)

        dto.session = entity.session?.let { trainingSessionMapper.toDto(it) }

        return dto
    }
    override fun toEntity(dto: TrainingDayDTO): TrainingDay {
        return modelMapper.map(dto, TrainingDay::class.java)
    }
}