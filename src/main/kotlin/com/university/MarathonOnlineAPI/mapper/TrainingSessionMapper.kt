package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingSessionDTO
import com.university.MarathonOnlineAPI.entity.TrainingSession
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingSessionMapper(private val modelMapper: ModelMapper): Mapper<TrainingSessionDTO, TrainingSession> {

    override fun toDto(entity: TrainingSession): TrainingSessionDTO {
        return modelMapper.map(entity, TrainingSessionDTO::class.java)
    }

    override fun toEntity(dto: TrainingSessionDTO): TrainingSession {
        val trainingSession = modelMapper.map(dto, TrainingSession::class.java)
        return trainingSession
    }
}