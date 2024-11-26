package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import com.university.MarathonOnlineAPI.entity.Registration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.time.LocalDateTime

class RaceMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var raceMapper: RaceMapper

    private lateinit var race: Race
    private lateinit var raceDTO: RaceDTO
    private lateinit var registration: Registration

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        registration = Registration(
            id = 1L,
            runner = null,
            payment = null,
            registrationDate = LocalDateTime.now(),
            completedDate = LocalDateTime.now(),
            registrationRank = 1,
            raceResults = null,
            rewards = null,
            status = null,
            contest = null
        )

        race = Race(
            id = 1L,
            distance = 42.195,
            timeTaken = 10800L, // 3 hours in seconds
            avgSpeed = 14.0,
            timestamp = LocalDateTime.now(),
            registration = registration
        )

        raceDTO = RaceDTO(
            id = 1L,
            distance = 42.195,
            timeTaken = 10800L,
            avgSpeed = 14.0,
            timestamp = LocalDateTime.now()
        )
    }

    @Test
    fun `should map Race to RaceDTO`() {
        Mockito.`when`(modelMapper.map(race, RaceDTO::class.java)).thenReturn(raceDTO)

        val result = raceMapper.toDto(race)

        assertEquals(race.id, result.id)
        assertEquals(race.distance, result.distance)
        assertEquals(race.timeTaken, result.timeTaken)
        assertEquals(race.avgSpeed, result.avgSpeed)
        assertEquals(race.timestamp, result.timestamp)
    }

    @Test
    fun `should map RaceDTO to Race`() {
        Mockito.`when`(modelMapper.map(raceDTO, Race::class.java)).thenReturn(race)

        val result = raceMapper.toEntity(raceDTO)

        assertEquals(raceDTO.id, result.id)
        assertEquals(raceDTO.distance, result.distance)
        assertEquals(raceDTO.timeTaken, result.timeTaken)
        assertEquals(raceDTO.avgSpeed, result.avgSpeed)
        assertEquals(raceDTO.timestamp?.second, result.timestamp?.second)
    }
}
