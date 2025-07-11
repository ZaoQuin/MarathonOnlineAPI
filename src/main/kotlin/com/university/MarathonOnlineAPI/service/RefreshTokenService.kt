package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.UserDTO
import org.springframework.security.core.userdetails.UserDetails

interface RefreshTokenService {
    fun findUserDetailsByToken(token: String): UserDetails?
    fun save(token: String, userDetails: UserDetails): UserDTO
}