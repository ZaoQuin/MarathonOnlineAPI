package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreateRuleRequest
import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule

interface RuleService {
    fun addRule(newRule: RuleDTO): Rule
    fun deleteRuleById(id: Long)
    fun updateRule(ruleDTO: RuleDTO): RuleDTO
    fun getRules(): List<RuleDTO>
    fun getById(id: Long): RuleDTO
}