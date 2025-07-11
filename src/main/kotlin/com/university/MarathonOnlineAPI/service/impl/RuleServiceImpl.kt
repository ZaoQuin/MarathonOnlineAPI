package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule
import com.university.MarathonOnlineAPI.exception.RuleException
import com.university.MarathonOnlineAPI.mapper.RuleMapper
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.RuleRepository
import com.university.MarathonOnlineAPI.service.RuleService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RuleServiceImpl(
    private val ruleRepository: RuleRepository,
    private val ruleMapper: RuleMapper,
    private val contestRepository: ContestRepository
) : RuleService {

    private val logger = LoggerFactory.getLogger(RuleServiceImpl::class.java)

    override fun addRule(newRule: RuleDTO): RuleDTO {

        val rule = Rule(
            icon = newRule.icon,
            name = newRule.name,
            description = newRule.description,
            updateDate = newRule.updateDate,
        )

        logger.debug("Saving rule: $rule")
        val savedRule = ruleRepository.save(rule)
        return ruleMapper.toDto(savedRule)
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
            val existingRule = ruleRepository.findById(ruleDTO.id ?: throw RuleException("Rule ID must not be null"))
                .orElseThrow { RuleException("Rule with ID ${ruleDTO.id} not found") }

            // Cập nhật các thuộc tính của existingRule
            existingRule.icon = ruleDTO.icon
            existingRule.name = ruleDTO.name
            existingRule.description = ruleDTO.description
            existingRule.updateDate = ruleDTO.updateDate

            // Lưu quy tắc đã cập nhật
            val updatedRule = ruleRepository.save(existingRule)

            // Chuyển đổi lại thành DTO để trả về
            ruleMapper.toDto(updatedRule)
        } catch (e: DataAccessException) {
            logger.error("Error updating rule: ${e.message}")
            throw RuleException("Database error occurred while updating rule: ${e.message}")
        }
    }

    override fun getRules(): List<RuleDTO> {
        return try {
            val rules = ruleRepository.findAll()
            rules.map { payment ->
                RuleDTO(
                    id = payment.id,
                    name = payment.name,
                    icon = payment.icon,
                    description = payment.description,
                    updateDate = payment.updateDate
                )
            }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving rules: ${e.message}")
            throw RuleException("Database error occurred while retrieving rules: ${e.message}")
        }
    }

    override fun getById(id: Long): RuleDTO {
        return try {
            // Fetch the Rule entity from the repository
            val rule = ruleRepository.findById(id)
                .orElseThrow { RuleException("Rule with ID $id not found") }

            // Map the Rule entity to RuleDTO
            RuleDTO(
                id = rule.id,
                icon = rule.icon,
                name = rule.name,
                description = rule.description,
                updateDate = rule.updateDate
            )
        } catch (e: DataAccessException) {
            logger.error("Error fetching rule with ID $id: ${e.message}")
            throw RuleException("Database error occurred while fetching rule: ${e.message}")
        }
    }
}
