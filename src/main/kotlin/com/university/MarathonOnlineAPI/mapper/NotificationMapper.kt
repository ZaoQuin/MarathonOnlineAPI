package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.Notification
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class NotificationMapper(private val modelMapper: ModelMapper, private val contestMapper: ContestMapper, private val userMapper: UserMapper): Mapper<NotificationDTO, Notification> {
    override fun toDto(entity: Notification): NotificationDTO {
        return modelMapper.map(entity, NotificationDTO::class.java)
    }

    override fun toEntity(dto: NotificationDTO): Notification {
        return modelMapper.map(dto, Notification::class.java)
    }
}