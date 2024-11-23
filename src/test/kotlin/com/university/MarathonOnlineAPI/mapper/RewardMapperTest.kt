package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RewardDTO
import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.ERewardType
import com.university.MarathonOnlineAPI.entity.Registration
import com.university.MarathonOnlineAPI.entity.Reward
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper

class RewardMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var rewardMapper: RewardMapper

    private lateinit var reward: Reward
    private lateinit var rewardDTO: RewardDTO
    private lateinit var contest: Contest
    private lateinit var registration: Registration

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        contest = Contest(id = 1L, name = "Contest 1")
        registration = Registration()

        reward = Reward(
            id = 1L,
            name = "Gold Medal",
            description = "First place reward",
            rewardRank = 1,
            type = ERewardType.PHYSICAL,
            isClaim = true,
            contest = contest,
            registration = registration
        )

        rewardDTO = RewardDTO(
            id = 1L,
            name = "Gold Medal",
            description = "First place reward",
            rewardRank = 1,
            type = ERewardType.PHYSICAL,
            isClaim = true
        )
    }

    @Test
    fun `should map Reward to RewardDTO`() {
        Mockito.`when`(modelMapper.map(reward, RewardDTO::class.java)).thenReturn(rewardDTO)

        val result = rewardMapper.toDto(reward)

        assertEquals(reward.id, result.id)
        assertEquals(reward.name, result.name)
        assertEquals(reward.description, result.description)
        assertEquals(reward.rewardRank, result.rewardRank)
        assertEquals(reward.type, result.type)
        assertEquals(reward.isClaim, result.isClaim)
    }

    @Test
    fun `should map RewardDTO to Reward`() {
        Mockito.`when`(modelMapper.map(rewardDTO, Reward::class.java)).thenReturn(reward)

        val result = rewardMapper.toEntity(rewardDTO)

        assertEquals(rewardDTO.id, result.id)
        assertEquals(rewardDTO.name, result.name)
        assertEquals(rewardDTO.description, result.description)
        assertEquals(rewardDTO.rewardRank, result.rewardRank)
        assertEquals(rewardDTO.type, result.type)
        assertEquals(rewardDTO.isClaim, result.isClaim)
    }
}
