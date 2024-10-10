package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.math.BigDecimal
import java.time.LocalDateTime

class ContestMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var contestMapper: ContestMapper

    private lateinit var contest: Contest
    private lateinit var contestDTO: ContestDTO

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val rules = listOf(
            Rule(1L, "Rule 1"),
            Rule(2L, "Rule 2")
        )

        val rewards = listOf(
            Reward(1L, "1st Place Medal"),
            Reward(2L, "2nd Place Medal")
        )

        val registration = Registration(
            id = 1L,
            runner = User(id = 2L, fullName = "Trần Văn A"),
            payment = Payment(/* thông tin payment */),
            registrationDate = LocalDateTime.now(),
            completedDate = LocalDateTime.now(),
            rank = 1,
            raceResults = emptyList(),
            rewards = emptyList(),
            status = ERegistrationStatus.COMPLETED,
            contest = null
        )

        val registrationDTO = RegistrationDTO(
            id = 1L,
            runner = UserDTO(id = 2L, fullName = "Trần Văn A"),
            payment = PaymentDTO(/* thông tin payment */),
            registrationDate = LocalDateTime.now(),
            completedDate = LocalDateTime.now(),
            rank = 1,
            raceResults = emptyList(),
            rewards = emptyList(),
            status = ERegistrationStatus.COMPLETED
        )

        contest = Contest(
            id = 1L,
            organizer = User(id = 1L, fullName = "Nguyễn Văn B"),
            name = "Marathon 2024",
            desc = "A great marathon event.",
            distance = 42.195,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now(),
            fee = BigDecimal("50.00"),
            maxMembers = 100,
            status = EContestStatus.ONGOING,
            createDate = LocalDateTime.now(),
            rules = rules,
            rewards = rewards,
            registrations = listOf(registration),
            registrationDeadline = LocalDateTime.now()
        )

        contestDTO = ContestDTO(
            id = 1L,
            organizer = UserDTO(id = 1L, fullName = "Nguyễn Văn B"),
            name = "Marathon 2024",
            desc = "A great marathon event.",
            distance = 42.195,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now(),
            fee = BigDecimal("50.00"),
            maxMembers = 100,
            status = EContestStatus.ONGOING,
            createDate = LocalDateTime.now(),
            rules = listOf(
                RuleDTO(1L, "Rule 1"),
                RuleDTO(2L, "Rule 2")
            ),
            rewards = listOf(
                RewardDTO(1L, "1st Place Medal"),
                RewardDTO(2L, "2nd Place Medal")
            ),
            registrations = listOf(registrationDTO),
            registrationDeadline = LocalDateTime.now()
        )
    }

    @Test
    fun `should map Contest to ContestDTO`() {
        `when`(modelMapper.map(contest, ContestDTO::class.java)).thenReturn(contestDTO)

        val result = contestMapper.toDto(contest)

        assertEquals(contest.id, result.id)
        assertEquals(contest.organizer?.id, result.organizer?.id)
        assertEquals(contest.name, result.name)
        assertEquals(contest.desc, result.desc)
        assertEquals(contest.distance, result.distance)
        assertEquals(contest.fee, result.fee)
        assertEquals(contest.maxMembers, result.maxMembers)
        assertEquals(contest.status, result.status)

        assertEquals(contest.rules?.size, result.rules?.size)
        contest.rules?.forEachIndexed { index, rule ->
            assertEquals(rule.id, result.rules!![index].id)
        }

        assertEquals(contest.rewards?.size, result.rewards?.size)
        contest.rewards?.forEachIndexed { index, reward ->
            assertEquals(reward.id, result.rewards!![index].id)
        }

        assertEquals(contest.registrations?.size, result.registrations?.size)
        contest.registrations?.forEachIndexed { index, registration ->
            assertEquals(registration.id, result.registrations!![index].id)
            assertEquals(registration.runner?.id, result.registrations!![index].runner?.id)
            assertEquals(registration.runner?.fullName, result.registrations!![index].runner?.fullName)
            assertEquals(registration.rank, result.registrations!![index].rank)
            assertEquals(registration.raceResults, result.registrations!![index].raceResults)
            assertEquals(registration.rewards, result.registrations!![index].rewards)
            assertEquals(registration.status, result.registrations!![index].status)
        }
    }

    @Test
    fun `should map ContestDTO to Contest`() {
        `when`(modelMapper.map(contestDTO, Contest::class.java)).thenReturn(contest)

        val result = contestMapper.toEntity(contestDTO)

        assertEquals(contestDTO.id, result.id)
        assertEquals(contestDTO.organizer?.id, result.organizer?.id)
        assertEquals(contestDTO.name, result.name)
        assertEquals(contestDTO.desc, result.desc)
        assertEquals(contestDTO.distance, result.distance)
        assertEquals(contestDTO.fee, result.fee)
        assertEquals(contestDTO.maxMembers, result.maxMembers)
        assertEquals(contestDTO.status, result.status)

        assertEquals(contestDTO.rules?.size, result.rules?.size)
        contestDTO.rules?.forEachIndexed { index, rule ->
            assertEquals(rule.id, result.rules!![index].id)
            assertEquals(rule.id, result.rules!![index].id)
        }

        assertEquals(contestDTO.rewards?.size, result.rewards?.size)
        contestDTO.rewards?.forEachIndexed { index, reward ->
            assertEquals(reward.id, result.rewards!![index].id)
        }

        contestDTO.registrations?.forEachIndexed { index, registration ->
            assertEquals(registration.id, result.registrations!![index].id)
            assertEquals(registration.runner?.id, result.registrations!![index].runner?.id)
            assertEquals(registration.runner?.fullName, result.registrations!![index].runner?.fullName)
            assertEquals(registration.rank, result.registrations!![index].rank)
            assertEquals(registration.raceResults, result.registrations!![index].raceResults)
            assertEquals(registration.rewards, result.registrations!![index].rewards)
            assertEquals(registration.status, result.registrations!![index].status)
        }
    }
}
