package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.UserService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class UserServiceImpl(
    private val userRepos: UserRepository,
    private val userMapper: UserMapper
): UserService {
    override fun addUser(userDTO: UserDTO): UserDTO {
        if(userDTO.id != (-1).toLong())
            throw UserException("Id must be null or -1.")

        val user = userMapper.toEntity(userDTO)
        userRepos.save(user)
        return userMapper.toDTO(user)
    }

}