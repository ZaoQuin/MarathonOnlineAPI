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
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.*

class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userMapper: UserMapper

    @Mock
    private lateinit var encoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should add user successfully`() {
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

        `when`(userMapper.toEntity(userDTO)).thenReturn(user)
        `when`(userRepository.save(user)).thenReturn(user)
        `when`(userMapper.toDto(user)).thenReturn(userDTO)

//        val result = userService.addUser(userDTO)
//
//        assertEquals(userDTO, result)
//
//        assertEquals(user.id, result.id)
//        assertEquals(user.fullName, result.fullName)
//        assertEquals(user.email, result.email)
//        assertEquals(user.phoneNumber, result.phoneNumber)
//        assertEquals(user.gender, result.gender)
//        assertEquals(user.birthday, result.birthday)
//        assertEquals(user.username, result.username)
//        assertEquals(user.role, result.role)
//        assertEquals(user.isVerified, result.isVerified)

        verify(userMapper).toEntity(userDTO)
        verify(userRepository).save(user)
        verify(userMapper).toDto(user)
    }


    @Test
    fun `should throw UserException when an error occurs`() {
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

        `when`(userMapper.toEntity(userDTO)).thenThrow(RuntimeException("Unexpected error"))

        val exception = assertThrows<UserException> {
//            userService.addUser(userDTO)
        }
        assertEquals("Error adding user: Unexpected error", exception.message)

        verify(userMapper).toEntity(userDTO)
    }

    @Test
    fun `should delete user successfully`() {
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

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))

        userService.deleteUserById(1L)

        verify(userRepository).findById(1L)
        verify(userRepository).delete(user)
    }

    @Test
    fun `should throw UserException when user not found`() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<UserException> {
            userService.deleteUserById(1L)
        }

        assertEquals("Error deleting user: User not found with ID: 1", exception.message)

        verify(userRepository).findById(1L)
        verify(userRepository, never()).delete(any())
    }

    @Test
    fun `should update user successfully`() {
        val userDTO = UserDTO(
            id = 1L,
            fullName = "John Doe Updated",
            email = "john.doe.updated@example.com",
            phoneNumber = "0987654321",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            role = ERole.RUNNER,
            isVerified = true
        )

        val existingUser = User(
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

        val updatedUser = User(
            id = 1L,
            fullName = "John Doe Updated",
            email = "john.doe.updated@example.com",
            phoneNumber = "0987654321",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            password = "password123",
            role = ERole.RUNNER,
            isVerified = true
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(updatedUser)).thenReturn(updatedUser)
        `when`(userMapper.toDto(updatedUser)).thenReturn(userDTO)

        val result = userService.updateUser(userDTO)

        assertEquals(userDTO, result)
        assertEquals(updatedUser.fullName, result.fullName)
        assertEquals(updatedUser.email, result.email)
        assertEquals(updatedUser.phoneNumber, result.phoneNumber)

        verify(userRepository).findById(1L)
        verify(userRepository).save(existingUser)
        verify(userMapper).toDto(updatedUser)
    }

    @Test
    fun `should throw UserException when user not found (update)`() {
        val userDTO = UserDTO(
            id = 1L,
            fullName = "John Doe Updated",
            email = "john.doe.updated@example.com",
            phoneNumber = "0987654321",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            role = ERole.RUNNER,
            isVerified = true
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<UserException> {
            userService.updateUser(userDTO)
        }

        assertEquals("Error updating user: User not found with ID: 1", exception.message)

        verify(userRepository).findById(1L)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `should return user DTO when user exists`() {
        val userId = 1L
        val user = User(
            id = userId,
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

        val userDTO = UserDTO(
            id = userId,
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            gender = EGender.MALE,
            birthday = LocalDate.of(1990, 1, 1),
            username = "johndoe",
            role = ERole.RUNNER,
            isVerified = true
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(userMapper.toDto(user)).thenReturn(userDTO)

        val result = userService.getById(userId)

        assertEquals(userDTO, result)
        verify(userRepository).findById(userId)
        verify(userMapper).toDto(user)
    }

    @Test
    fun `should throw UserException when user does not exist`() {
        val userId = 1L

        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        val exception = assertThrows<UserException> {
            userService.getById(userId)
        }

        assertEquals("Unable to retrieve user with ID: $userId", exception.message)
        verify(userRepository).findById(userId)
    }
}
