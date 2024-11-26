package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class RuleMapper (private val modelMapper: ModelMapper): Mapper<RuleDTO, Rule> {
    override fun toDto(entity: Rule): RuleDTO {
        val ruleDTO = modelMapper.map(entity, RuleDTO::class.java)
        return ruleDTO
    }
    override fun toEntity(dto: RuleDTO): Rule {
        val rule = modelMapper.map(dto, Rule::class.java)
        return rule
    }
}
