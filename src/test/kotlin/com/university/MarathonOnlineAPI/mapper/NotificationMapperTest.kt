package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
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

class NotificationMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var notificationMapper: NotificationMapper

    private lateinit var notification: Notification
    private lateinit var notificationDTO: NotificationDTO

    private lateinit var user: User
    private lateinit var contest: Contest
    private lateinit var userDTO: UserDTO
    private lateinit var contestDTO: ContestDTO

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        user = User(id = 1L, fullName = "John Doe", email = "john@example.com")
        contest = Contest(id = 1L, name = "Annual Marathon", startDate = LocalDateTime.now(), endDate = LocalDateTime.now())

        userDTO = UserDTO(id = 1L, fullName = "John Doe", email = "john@example.com")
        contestDTO = ContestDTO(id = 1L, name = "Annual Marathon", startDate = LocalDateTime.now(), endDate = LocalDateTime.now())

        notification = Notification(
            id = 1L,
            receiver = user,
            contest = contest,
            title = "New Contest Notification",
            content = "You are invited to the Annual Marathon",
            createAt = LocalDateTime.now(),
            isRead = false,
            type = ENotificationType.NEW_NOTIFICATION
        )

        notificationDTO = NotificationDTO(
            id = 1L,
            receiver = userDTO,
            contest = contestDTO,
            title = "New Contest Notification",
            content = "You are invited to the Annual Marathon",
            createAt = LocalDateTime.now(),
            isRead = false,
            type = ENotificationType.NEW_NOTIFICATION
        )
    }

    @Test
    fun `should map Notification to NotificationDTO`() {
        Mockito.`when`(modelMapper.map(notification, NotificationDTO::class.java)).thenReturn(notificationDTO)

        val result = notificationMapper.toDto(notification)

        assertEquals(notification.id, result.id)
        assertEquals(notification.receiver?.id, result.receiver?.id)
        assertEquals(notification.contest?.id, result.contest?.id)
        assertEquals(notification.title, result.title)
        assertEquals(notification.content, result.content)
        assertEquals(notification.createAt?.second, result.createAt?.second)
        assertEquals(notification.isRead, result.isRead)
        assertEquals(notification.type, result.type)
    }

    @Test
    fun `should map NotificationDTO to Notification`() {
        Mockito.`when`(modelMapper.map(notificationDTO, Notification::class.java)).thenReturn(notification)

        val result = notificationMapper.toEntity(notificationDTO)

        assertEquals(notificationDTO.id, result.id)
        assertEquals(notificationDTO.receiver?.id, result.receiver?.id)
        assertEquals(notificationDTO.contest?.id, result.contest?.id)
        assertEquals(notificationDTO.title, result.title)
        assertEquals(notificationDTO.content, result.content)
        assertEquals(notificationDTO.createAt, result.createAt)
        assertEquals(notificationDTO.isRead, result.isRead)
        assertEquals(notificationDTO.type, result.type)
    }
}
