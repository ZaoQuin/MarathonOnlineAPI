package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.Contest
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class ContestMapper(
    private val modelMapper: ModelMapper,
    private val userMapper: UserMapper
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

    override fun toEntity(dto: ContestDTO): Contest {
        return modelMapper.map(dto, Contest::class.java)
    }
}