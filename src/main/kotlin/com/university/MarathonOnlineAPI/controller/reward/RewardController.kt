package com.university.MarathonOnlineAPI.controller.reward

import com.university.MarathonOnlineAPI.dto.CreateRewardRequest
import com.university.MarathonOnlineAPI.dto.RewardDTO
import com.university.MarathonOnlineAPI.exception.RewardException
import com.university.MarathonOnlineAPI.service.RewardService

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.log

@RestController
@RequestMapping("/api/v1/reward")
class RewardController(private val rewardService: RewardService) {

    private val logger = LoggerFactory.getLogger(RewardController::class.java)

    @PostMapping
    fun addReward(@RequestBody @Valid newReward: CreateRewardRequest): ResponseEntity<Any> {
        return try {
            val addedReward = rewardService.addReward(newReward)
            logger.info("Show newReward: $newReward")
            ResponseEntity(addedReward, HttpStatus.CREATED)
        } catch (e: RewardException) {
            logger.error("Error adding reward: ${e.message}")
            ResponseEntity("Reward error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteReward(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            rewardService.deleteRewardById(id)
            logger.info("Reward with ID $id deleted successfully")
            ResponseEntity.ok("Reward with ID $id deleted successfully")
        } catch (e: RewardException) {
            logger.error("Failed to delete reward with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete reward with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete reward with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete reward with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateReward(@RequestBody @Valid rewardDTO: RewardDTO): ResponseEntity<RewardDTO> {
        return try {
            val updatedReward = rewardService.updateReward(rewardDTO)
            ResponseEntity(updatedReward, HttpStatus.OK)
        } catch (e: RewardException) {
            logger.error("Reward exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RewardException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating reward: ${e.message}")
            throw RewardException("Error updating reward: ${e.message}")
        }
    }

    @GetMapping
    fun getRewards(): ResponseEntity<List<RewardDTO>> {
        return try {
            val rewards = rewardService.getRewards()
            ResponseEntity(rewards, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRewards: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getRewardById(@PathVariable id: Long): ResponseEntity<RewardDTO> {
        return try {
            val foundReward = rewardService.getById(id)
            ResponseEntity.ok(foundReward)
        } catch (e: RewardException) {
            logger.error("Error getting reward by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting reward by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
