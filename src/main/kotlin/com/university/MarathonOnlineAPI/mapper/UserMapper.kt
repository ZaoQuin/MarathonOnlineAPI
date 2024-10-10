package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.User
import org.springframework.stereotype.Service

@Service
class UserMapper: Mapper<UserDTO, User> {
    override fun toDTO(entity: User): UserDTO = UserDTO(
        entity.id,
        entity.fullName,
        entity.email,
        entity.phoneNumber,
        entity.gender,
        entity.birthday,
        entity.username,
        entity.password,
        entity.role,
        entity.isVerified
    )

    override fun toEntity(dto: UserDTO): User = User(
        dto.id,
        dto.fullName,
        dto.email,
        dto.phoneNumber,
        dto.gender,
        dto.birthday,
        dto.username,
        dto.password,
        dto.role,
        dto.isVerified
    )
}