import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.EGender
import com.university.MarathonOnlineAPI.entity.ERole
import com.university.MarathonOnlineAPI.entity.User
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.impl.UserServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDate

class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userMapper: UserMapper

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should add user successfully`() {
        // Arrange
        val userDTO = UserDTO(
            id = 1L,
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            role = ERole.RUNNER,
            isVerified = true
        )

        val user = User(
            id = 1L,
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            password = "password123",
            role = ERole.RUNNER,
            isVerified = true
        )

        // Act
        `when`(userMapper.toEntity(userDTO)).thenReturn(user)
        `when`(userRepository.save(user)).thenReturn(user)
        `when`(userMapper.toDto(user)).thenReturn(userDTO)

        // Assert
        val result = userService.addUser(userDTO)

        assertEquals(userDTO, result)

        assertEquals(user.id, result.id)
        assertEquals(user.fullName, result.fullName)
        assertEquals(user.email, result.email)
        assertEquals(user.phoneNumber, result.phoneNumber)
        assertEquals(user.gender, result.gender)
        assertEquals(user.birthday, result.birthday)
        assertEquals(user.username, result.username)
        assertEquals(user.role, result.role)
        assertEquals(user.isVerified, result.isVerified)

        // Kiểm tra các phương thức đã được gọi
        verify(userMapper).toEntity(userDTO)
        verify(userRepository).save(user)
        verify(userMapper).toDto(user)
    }


    @Test
    fun `should throw UserException when an error occurs`() {
        // Arrange
        val userDTO = UserDTO(
            id = 1L,
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            role = ERole.RUNNER,
            isVerified = true
        )

        // Act
        `when`(userMapper.toEntity(userDTO)).thenThrow(RuntimeException("Unexpected error"))

        // Assert
        val exception = assertThrows<UserException> {
            userService.addUser(userDTO)
        }
        assertEquals("Unexpected error", exception.message)

        verify(userMapper).toEntity(userDTO)
    }
}
