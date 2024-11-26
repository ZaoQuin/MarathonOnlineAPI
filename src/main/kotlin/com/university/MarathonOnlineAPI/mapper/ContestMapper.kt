package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class ContestMapper(
    private val modelMapper: ModelMapper,
    private val userMapper: UserMapper,
    private val ruleMapper: RuleMapper, // Đảm bảo bạn có một RuleMapper để chuyển đổi các Rule
    private val rewardMapper: RewardMapper, // Tương tự cho RewardMapper
    private val registrationMapper: RegistrationMapper // Tương tự cho RegistrationMapper
): Mapper<ContestDTO, Contest> {
    override fun toDto(entity: Contest): ContestDTO {
        val contestDTO = modelMapper.map(entity, ContestDTO::class.java)
        contestDTO.organizer = entity.organizer?.let {userMapper.toDto(it)}
        contestDTO.rules = entity.rules?.map { rule ->
            ruleMapper.toDto(rule)
        }
        contestDTO.rewards = entity.rewards?.map { reward ->
            rewardMapper.toDto(reward)
        }
        contestDTO.registrations = entity.registrations?.map { registration ->
            registrationMapper.toDto(registration)
        }
        return contestDTO
    }
//    override fun toDto(entity: Contest): ContestDTO {
//        return modelMapper.map(entity, ContestDTO::class.java)
//    }

//    override fun toEntity(dto: ContestDTO): Contest {
//        val contest = modelMapper.map(dto, Contest::class.java)
//
//        contest.rules = dto.rules?.map { ruleMapper.toEntity(it) } ?: emptyList()
//        contest.rewards = dto.rewards?.map { rewardMapper.toEntity(it) } ?: emptyList()
//        contest.registrations = dto.registrations?.map { registrationMapper.toEntity(it) } ?: emptyList()
//
//        contest.organizer = dto.organizer?.let { userMapper.toEntity(it) }
//
//        return contest
//    }
    override fun toEntity(dto: ContestDTO): Contest {
        val contest = modelMapper.map(dto, Contest::class.java)
        contest.organizer = dto.organizer?.let {userMapper.toEntity(it)}
        contest.rules = dto.rules?.map { rule ->
            ruleMapper.toEntity(rule)
        }
        contest.rewards = dto.rewards?.map { reward ->
            rewardMapper.toEntity(reward)
        }
        contest.registrations = dto.registrations?.map { registration ->
            registrationMapper.toEntity(registration)
        }
        return contest
    }
}