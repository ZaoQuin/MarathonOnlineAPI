package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.User
import com.university.MarathonOnlineAPI.entity.EGender
import com.university.MarathonOnlineAPI.entity.ERole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.time.LocalDate

class UserMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var userMapper: UserMapper

    private lateinit var user: User
    private lateinit var userDTO: UserDTO

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        user = User(
            id = 1L,
            fullName = "Nguyễn Văn A",
            email = "nguyenvana@example.com",
            phoneNumber = "0123456789",
            gender = EGender.MALE,
            birthday = LocalDate.now(),
            username = "nguyenvana",
            password = "password",
            role = ERole.RUNNER,
            isVerified = true
        )

        userDTO = UserDTO(
            id = 1L,
            fullName = "Nguyễn Văn A",
            email = "nguyenvana@example.com",
            phoneNumber = "0123456789",
            gender = EGender.MALE,
            birthday = LocalDate.now(),
            username = "nguyenvana",
            password = "password",
            role = ERole.RUNNER,
            isVerified = true
        )
    }

    @Test
    fun `should map User to UserDTO`() {
        Mockito.`when`(modelMapper.map(user, UserDTO::class.java)).thenReturn(userDTO)

        val result = userMapper.toDto(user)

        assertEquals(user.id, result.id)
        assertEquals(user.fullName, result.fullName)
        assertEquals(user.email, result.email)
        assertEquals(user.phoneNumber, result.phoneNumber)
        assertEquals(user.gender, result.gender)
        assertEquals(user.username, result.username)
        assertEquals(user.password, result.password)
        assertEquals(user.role, result.role)
        assertEquals(user.isVerified, result.isVerified)
    }

    @Test
    fun `should map UserDTO to User`() {
        Mockito.`when`(modelMapper.map(userDTO, User::class.java)).thenReturn(user)

        val result = userMapper.toEntity(userDTO)

        assertEquals(userDTO.id, result.id)
        assertEquals(userDTO.fullName, result.fullName)
        assertEquals(userDTO.email, result.email)
        assertEquals(userDTO.phoneNumber, result.phoneNumber)
        assertEquals(userDTO.gender, result.gender)
        assertEquals(userDTO.username, result.username)
        assertEquals(userDTO.password, result.password)
        assertEquals(userDTO.role, result.role)
        assertEquals(userDTO.isVerified, result.isVerified)
    }
}
