package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.exception.RuleException
import com.university.MarathonOnlineAPI.mapper.RuleMapper
import com.university.MarathonOnlineAPI.repos.RuleRepository
import com.university.MarathonOnlineAPI.service.RuleService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RuleServiceImpl(
    private val ruleRepository: RuleRepository,
    private val ruleMapper: RuleMapper
) : RuleService {

    private val logger = LoggerFactory.getLogger(RuleServiceImpl::class.java)

    override fun addRule(newRule: RuleDTO): RuleDTO {
        logger.info("Received RuleDTO: $newRule")
        return try {
            val ruleEntity = ruleMapper.toEntity(newRule)
            val savedRule = ruleRepository.save(ruleEntity)
            ruleMapper.toDto(savedRule)
        } catch (e: DataAccessException) {
            logger.error("Error saving rule: ${e.message}")
            throw RuleException("Database error occurred while saving rule: ${e.message}")
        }
    }

    override fun deleteRuleById(id: Long) {
        try {
            ruleRepository.deleteById(id)
            logger.info("Rule with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting rule with ID $id: ${e.message}")
            throw RuleException("Database error occurred while deleting rule: ${e.message}")
        }
    }

    override fun updateRule(ruleDTO: RuleDTO): RuleDTO {
        return try {
            val ruleEntity = ruleMapper.toEntity(ruleDTO)
            val updatedRule = ruleRepository.save(ruleEntity)
            ruleMapper.toDto(updatedRule)
        } catch (e: DataAccessException) {
            logger.error("Error updating rule: ${e.message}")
            throw RuleException("Database error occurred while updating rule: ${e.message}")
        }
    }

    override fun getRules(): List<RuleDTO> {
        return try {
            val rules = ruleRepository.findAll()
            rules.map { ruleMapper.toDto(it) }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving rules: ${e.message}")
            throw RuleException("Database error occurred while retrieving rules: ${e.message}")
        }
    }

    override fun getById(id: Long): RuleDTO {
        return try {
            val rule = ruleRepository.findById(id)
                .orElseThrow { RuleException("Rule with ID $id not found") }
            ruleMapper.toDto(rule)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving rule with ID $id: ${e.message}")
            throw RuleException("Database error occurred while retrieving rule: ${e.message}")
        }
    }
}
