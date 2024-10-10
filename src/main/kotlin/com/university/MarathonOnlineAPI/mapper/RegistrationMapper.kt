package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.Registration
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RegistrationMapper(private val modelMapper: ModelMapper): Mapper<RegistrationDTO, Registration> {
    override fun toDto(entity: Registration): RegistrationDTO {
        return modelMapper.map(entity, RegistrationDTO::class.java)
    }

    override fun toEntity(dto: RegistrationDTO): Registration {
        return modelMapper.map(dto, Registration::class.java)
    }
}