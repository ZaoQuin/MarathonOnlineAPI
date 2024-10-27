package com.university.MarathonOnlineAPI.service

import org.springframework.security.core.userdetails.UserDetails

interface RefreshTokenService {
    fun findUserDetailsByToken(token: String): UserDetails?
    fun save(token: String, userDetails: UserDetails)
}