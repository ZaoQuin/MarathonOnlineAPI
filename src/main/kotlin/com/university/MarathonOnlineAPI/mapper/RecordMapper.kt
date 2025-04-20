package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.Record
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RecordMapper(private val modelMapper: ModelMapper, private val userMapper: UserMapper) : Mapper<RecordDTO, Record> {

    override fun toDto(entity: Record): RecordDTO {
        return modelMapper.map(entity, RecordDTO::class.java)
    }

    override fun toEntity(dto: RecordDTO): Record {
        return modelMapper.map(dto, Record::class.java)
    }
}