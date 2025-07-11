package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.Registration
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RegistrationMapper(private val modelMapper: ModelMapper): Mapper<RegistrationDTO, Registration> {
    override fun toDto(entity: Registration): RegistrationDTO {
        val registrationDTO = modelMapper.map(entity, RegistrationDTO::class.java)
        return registrationDTO
    }

    override fun toEntity(dto: RegistrationDTO): Registration {
        val registration = modelMapper.map(dto, Registration::class.java)
        return registration
    }
}