package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.Record
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RecordMapper(private val modelMapper: ModelMapper) : Mapper<RecordDTO, Record> {

    override fun toDto(entity: Record): RecordDTO {
        var dto = modelMapper.map(entity, RecordDTO::class.java)
        if (entity.startTime != null && entity.endTime != null) {
            val duration = Duration.between(entity.startTime, entity.endTime)
            dto.timeTaken = duration.seconds
        } else {
            dto.timeTaken = null
        }
        return dto
    }

    override fun toEntity(dto: RecordDTO): Record {
        return modelMapper.map(dto, Record::class.java)
    }
}