package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.Registration
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RegistrationMapper(private val modelMapper: ModelMapper, private val userMapper: UserMapper, private val paymentMapper: PaymentMapper, private val rewardMapper: RewardMapper, private val raceMapper: RaceMapper): Mapper<RegistrationDTO, Registration> {
    override fun toDto(entity: Registration): RegistrationDTO {
        val registrationDTO = modelMapper.map(entity, RegistrationDTO::class.java)
        registrationDTO.runner = entity.runner?.let {userMapper.toDto(it)}
        registrationDTO.payment = entity.payment?.let {paymentMapper.toDto(it)}
        registrationDTO.rewards = entity.rewards?.map { reward ->
            rewardMapper.toDto(reward)
        }
        registrationDTO.raceResults = entity.raceResults?.map {race ->
            raceMapper.toDto(race)
        }
        return registrationDTO
    }

    override fun toEntity(dto: RegistrationDTO): Registration {
        val registration = modelMapper.map(dto, Registration::class.java)
        registration.runner = dto.runner?.let {userMapper.toEntity(it)}
        registration.payment = dto.payment?.let {paymentMapper.toEntity(it)}
        registration.rewards = dto.rewards?.map { reward ->
            rewardMapper.toEntity(reward)
        }
        registration.raceResults = dto.raceResults?.map {race ->
            raceMapper.toEntity(race)
        }
        return registration
    }
}