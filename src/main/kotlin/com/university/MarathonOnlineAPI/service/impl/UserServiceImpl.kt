package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.UserRepository
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepos: UserRepository,
    private val userMapper: UserMapper
): UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun addUser(userDTO: UserDTO): UserDTO {
        logger.info("Received UserDTO: $userDTO")
        try {
            val user = userMapper.toEntity(userDTO)
            logger.info("Mapper to Entity: $user")
            userRepos.save(user)
            return userMapper.toDto(user)
        } catch (e: Exception){
            throw UserException(e.message)
        }
    }

}