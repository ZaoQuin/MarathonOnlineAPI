package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.entity.Notification
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class NotificationMapper(private val modelMapper: ModelMapper, private val contestMapper: ContestMapper, private val userMapper: UserMapper): Mapper<NotificationDTO, Notification> {
    override fun toDto(entity: Notification): NotificationDTO {
        val notificationDTO = modelMapper.map(entity, NotificationDTO::class.java)
        notificationDTO.contest = entity.contest?.let {contestMapper.toDto(it)}
        notificationDTO.receiver = entity.receiver?.let {userMapper.toDto(it)}
        return notificationDTO
    }

    override fun toEntity(dto: NotificationDTO): Notification {
        val notification = modelMapper.map(dto, Notification::class.java)
        notification.contest = dto.contest?.let {contestMapper.toEntity(it)}
        notification.receiver = dto.receiver?.let {userMapper.toEntity(it)}
        return notification
    }
}