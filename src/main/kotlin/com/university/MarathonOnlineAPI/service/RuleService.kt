package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RuleDTO

interface RuleService {
    fun addRule(newRule: RuleDTO): RuleDTO
    fun deleteRuleById(id: Long)
    fun updateRule(ruleDTO: RuleDTO): RuleDTO
    fun getRules(): List<RuleDTO>
    fun getById(id: Long): RuleDTO
}