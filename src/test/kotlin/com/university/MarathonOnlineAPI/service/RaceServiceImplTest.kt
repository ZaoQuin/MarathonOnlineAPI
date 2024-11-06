package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RaceDTO
import com.university.MarathonOnlineAPI.entity.Race
import com.university.MarathonOnlineAPI.exception.RaceException
import com.university.MarathonOnlineAPI.mapper.RaceMapper
import com.university.MarathonOnlineAPI.repos.RaceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime

class RaceServiceImplTest {

    @Mock
    private lateinit var raceRepository: RaceRepository

    @Mock
    private lateinit var raceMapper: RaceMapper

    @InjectMocks
    private lateinit var raceService: RaceServiceImpl

    private lateinit var raceDTO: RaceDTO
    private lateinit var race: Race

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Khởi tạo các đối tượng cần thiết cho bài kiểm tra
        raceDTO = RaceDTO(id = 1L, distance = 10.0, timeTaken = 3600L, avgSpeed = 10.0, timestamp = LocalDateTime.now())
        race = Race(id = 1L, distance = 10.0, timeTaken = 3600L, avgSpeed = 10.0, timestamp = LocalDateTime.now())
    }

    @Test
    fun `should add race successfully`() {
        `when`(raceMapper.toEntity(raceDTO)).thenReturn(race)
        `when`(raceRepository.save(race)).thenReturn(race)
        `when`(raceMapper.toDto(race)).thenReturn(raceDTO)

        val result = raceService.addRace(raceDTO)

        assertEquals(raceDTO, result)
        verify(raceRepository).save(race)
        verify(raceMapper).toEntity(raceDTO)
    }

    @Test
    fun `should throw RaceException when adding race fails`() {
        `when`(raceMapper.toEntity(raceDTO)).thenReturn(race)
        `when`(raceRepository.save(race)).thenThrow(DataIntegrityViolationException("Database error"))

        val exception = assertThrows<RaceException> {
            raceService.addRace(raceDTO)
        }

        assertEquals("Database error occurred while saving race: Database error", exception.message)
    }

    @Test
    fun `should delete race successfully`() {
        `when`(raceRepository.existsById(1L)).thenReturn(true)

        raceService.deleteRaceById(1L)

        verify(raceRepository).deleteById(1L)
    }

    @Test
    fun `should throw RaceException when deleting non-existent race`() {
        `when`(raceRepository.existsById(1L)).thenReturn(false)

        val exception = assertThrows<RaceException> {
            raceService.deleteRaceById(1L)
        }

        assertEquals("Race with ID 1 not found", exception.message)
    }

    @Test
    fun `should update race successfully`() {
        `when`(raceMapper.toEntity(raceDTO)).thenReturn(race)
        `when`(raceRepository.save(race)).thenReturn(race)
        `when`(raceMapper.toDto(race)).thenReturn(raceDTO)

        val result = raceService.updateRace(raceDTO)

        assertEquals(raceDTO, result)
        verify(raceRepository).save(race)
    }

    @Test
    fun `should throw RaceException when updating race fails`() {
        `when`(raceMapper.toEntity(raceDTO)).thenReturn(race)
        `when`(raceRepository.save(race)).thenThrow(DataIntegrityViolationException("Database error"))

        val exception = assertThrows<RaceException> {
            raceService.updateRace(raceDTO)
        }

        assertEquals("Database error occurred while updating race: Database error", exception.message)
    }

    @Test
    fun `should fetch all races successfully`() {
        val races = listOf(race)
        val raceDTOs = listOf(raceDTO)

        `when`(raceRepository.findAll()).thenReturn(races)
        `when`(raceMapper.toDto(race)).thenReturn(raceDTO)

        val result = raceService.getRaces()

        assertEquals(raceDTOs, result)
        verify(raceRepository).findAll()
    }

    @Test
    fun `should throw RaceException when fetching races fails`() {
        `when`(raceRepository.findAll()).thenThrow(DataIntegrityViolationException("Database error"))

        val exception = assertThrows<RaceException> {
            raceService.getRaces()
        }

        assertEquals("Database error occurred while fetching races: Database error", exception.message)
    }

    @Test
    fun `should fetch race by ID successfully`() {
        `when`(raceRepository.findById(1L)).thenReturn(java.util.Optional.of(race))
        `when`(raceMapper.toDto(race)).thenReturn(raceDTO)

        val result = raceService.getById(1L)

        assertEquals(raceDTO, result)
        verify(raceRepository).findById(1L)
    }

    @Test
    fun `should throw RaceException when fetching non-existent race by ID`() {
        `when`(raceRepository.findById(1L)).thenReturn(java.util.Optional.empty())

        val exception = assertThrows<RaceException> {
            raceService.getById(1L)
        }

        assertEquals("Race with ID 1 not found", exception.message)
    }

    @Test
    fun `should throw RaceException when fetching race by ID fails`() {
        `when`(raceRepository.findById(1L)).thenThrow(DataIntegrityViolationException("Database error"))

        val exception = assertThrows<RaceException> {
            raceService.getById(1L)
        }

        assertEquals("Database error occurred while fetching race: Database error", exception.message)
    }
}
