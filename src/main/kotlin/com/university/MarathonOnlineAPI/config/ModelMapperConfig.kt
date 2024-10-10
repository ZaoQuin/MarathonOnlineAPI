package com.university.MarathonOnlineAPI.config

import com.university.MarathonOnlineAPI.dto.RegistrationDTO
import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.Registration
import org.modelmapper.ModelMapper
import org.modelmapper.convention.MatchingStrategies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ModelMapperConfig {
    @Bean
    fun modelMapper(): ModelMapper{
        val modelMapper = ModelMapper()

        modelMapper.configuration.matchingStrategy = MatchingStrategies.LOOSE


        modelMapper.createTypeMap(RegistrationDTO::class.java, Registration::class.java).addMappings { mapper ->
            mapper.skip { registration: Registration, _: Contest? -> registration.contest }
        }

        return modelMapper
    }
}