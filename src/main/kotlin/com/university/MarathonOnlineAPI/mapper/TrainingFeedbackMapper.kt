package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingFeedbackDTO
import com.university.MarathonOnlineAPI.entity.TrainingFeedback
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component


@Component
class TrainingFeedbackMapper(private val modelMapper: ModelMapper): Mapper<TrainingFeedbackDTO, TrainingFeedback> {
    override fun toDto(entity: TrainingFeedback): TrainingFeedbackDTO {
        return modelMapper.map(entity, TrainingFeedbackDTO::class.java)
    }
    override fun toEntity(dto: TrainingFeedbackDTO): TrainingFeedback {
        return modelMapper.map(dto, TrainingFeedback::class.java)
    }
}