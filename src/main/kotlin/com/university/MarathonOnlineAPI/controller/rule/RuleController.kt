package com.university.MarathonOnlineAPI.controller.rule

import com.university.MarathonOnlineAPI.dto.CreateRewardRequest
import com.university.MarathonOnlineAPI.dto.CreateRuleRequest
import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Rule
import com.university.MarathonOnlineAPI.exception.RuleException
import com.university.MarathonOnlineAPI.service.RuleService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/rule")
class RuleController(private val ruleService: RuleService) {

    private val logger = LoggerFactory.getLogger(RuleController::class.java)

    @PostMapping
    fun addRule(@RequestBody @Valid newRule: RuleDTO): ResponseEntity<Any> {
        return try {
            val addedRule = ruleService.addRule(newRule)
            logger.info("Show newRule: $newRule")
            ResponseEntity(addedRule, HttpStatus.CREATED)
        } catch (e: RuleException) {
            logger.error("Error adding rule: ${e.message}")
            ResponseEntity("Rule error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteRule(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            ruleService.deleteRuleById(id)
            logger.info("Rule with ID $id deleted successfully")
            ResponseEntity.ok("Rule with ID $id deleted successfully")
        } catch (e: RuleException) {
            logger.error("Failed to delete rule with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete rule with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete rule with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete rule with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateRule(@RequestBody @Valid ruleDTO: RuleDTO): ResponseEntity<RuleDTO> {
        return try {
            val updatedRule = ruleService.updateRule(ruleDTO)
            ResponseEntity(updatedRule, HttpStatus.OK)
        } catch (e: RuleException) {
            logger.error("Rule exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RuleException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating rule: ${e.message}")
            throw RuleException("Error updating rule: ${e.message}")
        }
    }

    @GetMapping
    fun getRules(): ResponseEntity<List<RuleDTO>> {
        return try {
            val rules = ruleService.getRules()
            ResponseEntity(rules, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRules: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getRuleById(@PathVariable id: Long): ResponseEntity<RuleDTO> {
        return try {
            val foundRule = ruleService.getById(id)
            ResponseEntity.ok(foundRule)
        } catch (e: RuleException) {
            logger.error("Error getting rule by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting rule by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}