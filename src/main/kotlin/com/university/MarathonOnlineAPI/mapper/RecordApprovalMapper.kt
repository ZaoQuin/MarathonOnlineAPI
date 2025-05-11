package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RecordApprovalDTO
import com.university.MarathonOnlineAPI.entity.RecordApproval
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RecordApprovalMapper(private val modelMapper: ModelMapper) : Mapper<RecordApprovalDTO, RecordApproval> {
    override fun toDto(entity: RecordApproval): RecordApprovalDTO {
        return modelMapper.map(entity, RecordApprovalDTO::class.java)
    }

    override fun toEntity(dto: RecordApprovalDTO): RecordApproval {
        return modelMapper.map(dto, RecordApproval::class.java)
    }
}