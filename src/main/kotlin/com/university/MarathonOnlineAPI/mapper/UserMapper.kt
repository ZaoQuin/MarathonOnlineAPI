package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.entity.User
import com.university.MarathonOnlineAPI.dto.UserDTO
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class UserMapper(private val modelMapper: ModelMapper): Mapper<UserDTO, User> {

    override fun toDto(entity: User): UserDTO {
        return modelMapper.map(entity, UserDTO::class.java)
    }
    override fun toEntity(dto: UserDTO): User {
        return modelMapper.map(dto, User::class.java)
    }
}