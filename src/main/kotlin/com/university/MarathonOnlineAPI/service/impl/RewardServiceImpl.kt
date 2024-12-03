package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreateRewardRequest
import com.university.MarathonOnlineAPI.dto.RewardDTO
import com.university.MarathonOnlineAPI.entity.Reward
import com.university.MarathonOnlineAPI.exception.RewardException
import com.university.MarathonOnlineAPI.mapper.RewardMapper
import com.university.MarathonOnlineAPI.repos.RewardRepository
import com.university.MarathonOnlineAPI.service.RewardService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class RewardServiceImpl(
    private val rewardRepository: RewardRepository,
    private val rewardMapper: RewardMapper,
    private val paymentServiceImpl: PaymentServiceImpl
) : RewardService {

    private val logger = LoggerFactory.getLogger(RewardServiceImpl::class.java)

    override fun addReward(newReward: CreateRewardRequest): RewardDTO {
        logger.info("Received RewardDTO: $newReward")
        try {
            val reward = Reward()
            reward.name = newReward.name
            reward.description = newReward.description
            reward.rewardRank = newReward.rewardRank
            reward.type = newReward.type
            reward.isClaim = newReward.isClaim

            logger.info("Map to entity: $reward")
            val savedReward = rewardRepository.save(reward)
            return rewardMapper.toDto(savedReward)
        } catch (e: DataAccessException) {
            logger.error("Error saving reward: ${e.message}")
            throw RewardException("Database error occurred while saving reward: ${e.message}")
        }
    }

    override fun deleteRewardById(id: Long) {
        try {
            rewardRepository.deleteById(id)
            logger.info("Reward with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting reward with ID $id: ${e.message}")
            throw RewardException("Database error occurred while deleting reward: ${e.message}")
        }
    }

    override fun updateReward(rewardDTO: RewardDTO): RewardDTO {
        return try {
            val rewardEntity = rewardMapper.toEntity(rewardDTO)
            val updatedReward = rewardRepository.save(rewardEntity)
            rewardMapper.toDto(updatedReward)
        } catch (e: DataAccessException) {
            logger.error("Error updating reward: ${e.message}")
            throw RewardException("Database error occurred while updating reward: ${e.message}")
        }
    }

    override fun getRewards(): List<RewardDTO> {
        return try {
            val rewards = rewardRepository.findAll()
            rewards.map { reward ->
                RewardDTO(
                    id = reward.id,
                    name= reward.name,
                    description = reward.description,
                    rewardRank = reward.rewardRank,
                    type= reward.type,
                    isClaim = reward.isClaim
                )
            }
        } catch (e: DataAccessException) {
            logger.error("Error retrieving rewards: ${e.message}")
            throw RewardException("Database error occurred while retrieving rewards: ${e.message}")
        }
    }

    override fun getById(id: Long): RewardDTO {
        return try {
            val reward = rewardRepository.findById(id)
                .orElseThrow { RewardException("Reward with ID $id not found") }
            rewardMapper.toDto(reward)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving reward with ID $id: ${e.message}")
            throw RewardException("Database error occurred while retrieving reward: ${e.message}")
        }
    }
}
