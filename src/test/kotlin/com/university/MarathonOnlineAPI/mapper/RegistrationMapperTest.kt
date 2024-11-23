package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.*
import com.university.MarathonOnlineAPI.entity.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.time.LocalDateTime

class RegistrationMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var registrationMapper: RegistrationMapper

    private lateinit var registration: Registration
    private lateinit var registrationDTO: RegistrationDTO
    private lateinit var runner: User
    private lateinit var runnerDTO: UserDTO
    private lateinit var payment: Payment
    private lateinit var paymentDTO: PaymentDTO
    private lateinit var contest: Contest
    private lateinit var reward: Reward
    private lateinit var rewardDTO: RewardDTO
    private lateinit var race: Race
    private lateinit var raceDTO: RaceDTO

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        contest = Contest(id = 1L, name = "Marathon 2024")

        runner = User(id = 1L, fullName = "John Doe", email = "johndoe@example.com")
        runnerDTO = UserDTO(id = 1L, fullName = "John Doe", email = "johndoe@example.com")

        payment = Payment(id = 1L, amount = (1000000).toBigDecimal())
        paymentDTO = PaymentDTO(id = 1L, amount = (1000000).toBigDecimal())

        reward = Reward(id = 1L, name = "Gold Medal", description = "First Place", rewardRank = 1, type = ERewardType.PHYSICAL, isClaim = true)
        rewardDTO = RewardDTO(id = 1L, name = "Gold Medal", description = "First Place", rewardRank = 1, type = ERewardType.PHYSICAL, isClaim = true)

        race = Race(id = 1L, distance = 42.195, timeTaken = 1000)
        raceDTO = RaceDTO(id = 1L, distance = 42.195, timeTaken = 1000)

        registration = Registration(
            id = 1L,
            runner = runner,
            payment = payment,
            registrationDate = LocalDateTime.now(),
            completedDate = LocalDateTime.now(),
            registrationRank = 1,
            raceResults = listOf(race),
            rewards = listOf(reward),
            status = ERegistrationStatus.COMPLETED,
            contest = contest
        )

        registrationDTO = RegistrationDTO(
            id = 1L,
            runner = runnerDTO,
            payment = paymentDTO,
            registrationDate = LocalDateTime.now(),
            completedDate = LocalDateTime.now(),
            registrationrank = 1,
            raceResults = listOf(raceDTO),
            rewards = listOf(rewardDTO),
            status = ERegistrationStatus.COMPLETED
        )
    }

    @Test
    fun `should map Registration to RegistrationDTO`() {
        Mockito.`when`(modelMapper.map(registration, RegistrationDTO::class.java)).thenReturn(registrationDTO)

        val result = registrationMapper.toDto(registration)

        assertEquals(registration.id, result.id)
        assertEquals(registration.runner?.id, result.runner?.id)
        assertEquals(registration.payment?.id, result.payment?.id)
        assertEquals(registration.registrationDate, result.registrationDate)
        assertEquals(registration.completedDate, result.completedDate)
        assertEquals(registration.registrationRank, result.registrationrank)
        assertEquals(registration.raceResults?.size, result.raceResults?.size)
        assertEquals(registration.rewards?.size, result.rewards?.size)
        assertEquals(registration.status, result.status)
    }

    @Test
    fun `should map RegistrationDTO to Registration`() {
        Mockito.`when`(modelMapper.map(registrationDTO, Registration::class.java)).thenReturn(registration)

        val result = registrationMapper.toEntity(registrationDTO)

        assertEquals(registrationDTO.id, result.id)
        assertEquals(registrationDTO.runner?.id, result.runner?.id)
        assertEquals(registrationDTO.payment?.id, result.payment?.id)
        assertEquals(registrationDTO.registrationDate?.second, result.registrationDate?.second)
        assertEquals(registrationDTO.completedDate?.second, result.completedDate?.second)
        assertEquals(registrationDTO.registrationrank, result.registrationRank)
        assertEquals(registrationDTO.raceResults?.size, result.raceResults?.size)
        assertEquals(registrationDTO.rewards?.size, result.rewards?.size)
        assertEquals(registrationDTO.status, result.status)
    }
}
