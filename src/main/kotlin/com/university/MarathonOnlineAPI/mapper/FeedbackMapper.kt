package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.FeedbackDTO
import com.university.MarathonOnlineAPI.entity.Feedback
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class FeedbackMapper(private val modelMapper: ModelMapper) : Mapper<FeedbackDTO, Feedback> {
    override fun toDto(entity: Feedback): FeedbackDTO {
        return modelMapper.map(entity, FeedbackDTO::class.java)
    }

    override fun toEntity(dto: FeedbackDTO): Feedback {
        return modelMapper.map(dto, Feedback::class.java)
    }
}