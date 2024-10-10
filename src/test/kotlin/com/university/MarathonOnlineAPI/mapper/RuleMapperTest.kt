package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.RuleDTO
import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.time.LocalDateTime

class RuleMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var ruleMapper: RuleMapper

    private lateinit var rule: Rule
    private lateinit var ruleDTO: RuleDTO
    private lateinit var contest: Contest

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        contest = Contest(id = 1L, name = "Contest 1") // Assuming Contest has id and name

        rule = Rule(
            id = 1L,
            icon = "icon.png",
            name = "Rule 1",
            desc = "Description of Rule 1",
            updateDate = LocalDateTime.now(),
            contest = contest
        )

        ruleDTO = RuleDTO(
            id = 1L,
            icon = "icon.png",
            name = "Rule 1",
            desc = "Description of Rule 1",
            updateDate = LocalDateTime.now()
        )
    }

    @Test
    fun `should map Rule to RuleDTO`() {
        Mockito.`when`(modelMapper.map(rule, RuleDTO::class.java)).thenReturn(ruleDTO)

        val result = ruleMapper.toDto(rule)

        assertEquals(rule.id, result.id)
        assertEquals(rule.icon, result.icon)
        assertEquals(rule.name, result.name)
        assertEquals(rule.desc, result.desc)
        assertEquals(rule.updateDate, result.updateDate)
    }

    @Test
    fun `should map RuleDTO to Rule`() {
        Mockito.`when`(modelMapper.map(ruleDTO, Rule::class.java)).thenReturn(rule)

        val result = ruleMapper.toEntity(ruleDTO)

        assertEquals(ruleDTO.id, result.id)
        assertEquals(ruleDTO.icon, result.icon)
        assertEquals(ruleDTO.name, result.name)
        assertEquals(ruleDTO.desc, result.desc)
        assertEquals(ruleDTO.updateDate, result.updateDate)
    }
}
