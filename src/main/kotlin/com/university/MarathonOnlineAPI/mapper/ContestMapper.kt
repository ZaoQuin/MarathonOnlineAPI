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
        return ContestDTO(
            id = entity.id,
            organizer = entity.organizer?.let { userMapper.toDto(it) },
            name = entity.name,
            description = entity.description,
            distance = entity.distance,
            startDate = entity.startDate,
            endDate = entity.endDate,
            fee = entity.fee,
            maxMembers = entity.maxMembers,
            status = entity.status,
            createDate = entity.createDate,
            rules = entity.rules?.map { rule ->
                RuleDTO(
                    id = rule.id,
                    name = rule.name,
                    icon = rule.icon,
                    description = rule.description,
                    updateDate = rule.updateDate
                ) },
            rewards = entity.rewards?.map { reward ->
                RewardDTO(
                    id = reward.id,
                    name = reward.name,
                    description = reward.description,
                    rewardRank = reward.rewardRank,
                    type = reward.type,
                    isClaim = reward.isClaim
                )
            },
            registrations = entity.registrations?.map { registration ->
                RegistrationDTO(
                    id = registration.id,
                    runner = registration.runner?.let { userMapper.toDto(it) },
                    payment = PaymentDTO(
                        id = registration.payment?.id,
                        amount = registration.payment?.amount,
                        paymentDate = registration.payment?.paymentDate,
                        status = registration.payment?.status
                    ),
                    registrationDate = registration.registrationDate,
                    completedDate = registration.completedDate,
                    registrationRank = registration.registrationRank,
                    raceResults = registration.raceResults?.map { race -> RaceDTO(
                        id = race.id,
                        distance = race.distance,
                        timeTaken = race.timeTaken,
                        avgSpeed = race.avgSpeed,
                        timestamp = race.timestamp
                    )

                    },
                    rewards = registration.rewards?.map {reward -> RewardDTO(
                        id = reward.id,
                        name = reward.name,
                        description = reward.description,
                        rewardRank = reward.rewardRank,
                        type = reward.type,
                        isClaim = reward.isClaim
                    )
                    },
                    status = registration.status
                )
            },
            registrationDeadline = entity.registrationDeadline
        )
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
    return Contest(
        id = dto.id,
        organizer = dto.organizer?.let { userMapper.toEntity(it) }, // Chuyển đổi từ DTO về entity cho organizer
        name = dto.name,
        description = dto.description,
        distance = dto.distance,
        startDate = dto.startDate,
        endDate = dto.endDate,
        fee = dto.fee,
        maxMembers = dto.maxMembers,
        status = dto.status,
        createDate = dto.createDate,
        rules = dto.rules?.map { ruleDto ->
            Rule(
                id = ruleDto.id,
                name = ruleDto.name,
                icon = ruleDto.icon,
                description = ruleDto.description,
                updateDate = ruleDto.updateDate
            )
        },
        rewards = dto.rewards?.map { rewardDto ->
            Reward(
                id = rewardDto.id,
                name = rewardDto.name,
                description = rewardDto.description,
                rewardRank = rewardDto.rewardRank,
                type = rewardDto.type,
                isClaim = rewardDto.isClaim
            )
        },
        registrations = dto.registrations?.map { registrationDto ->
            Registration(
                id = registrationDto.id,
                runner = registrationDto.runner?.let { userMapper.toEntity(it) }, // Chuyển đổi từ DTO về entity cho runner
                payment = registrationDto.payment?.let { Payment(
                    id = it.id,
                    amount = it.amount,
                    paymentDate = it.paymentDate,
                    status = it.status
                ) },
                registrationDate = registrationDto.registrationDate,
                completedDate = registrationDto.completedDate,
                registrationRank = registrationDto.registrationRank,
                raceResults = registrationDto.raceResults?.map { raceDto ->
                    Race(
                        id = raceDto.id,
                        distance = raceDto.distance,
                        timeTaken = raceDto.timeTaken,
                        avgSpeed = raceDto.avgSpeed,
                        timestamp = raceDto.timestamp
                    )
                },
                rewards = registrationDto.rewards?.map { rewardDto ->
                    Reward(
                        id = rewardDto.id,
                        name = rewardDto.name,
                        description = rewardDto.description,
                        rewardRank = rewardDto.rewardRank,
                        type = rewardDto.type,
                        isClaim = rewardDto.isClaim
                    )
                },
                status = registrationDto.status
            )
        },
        registrationDeadline = dto.registrationDeadline
    )
}
}