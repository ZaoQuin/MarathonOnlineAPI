package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RuleMapper (private val modelMapper: ModelMapper): Mapper<RuleDTO, Rule> {
    override fun toDto(entity: Rule): RuleDTO {
        return modelMapper.map(entity, RuleDTO::class.java)
    }
    override fun toEntity(dto: RuleDTO): Rule {
        return modelMapper.map(dto, Rule::class.java)
    }
}
