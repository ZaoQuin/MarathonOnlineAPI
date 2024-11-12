package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.auth.AuthenticationRequest
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationResponse
import com.university.MarathonOnlineAPI.dto.UserDTO

interface AuthenticationService {
    fun authentication(authRequest: AuthenticationRequest): AuthenticationResponse
    fun refreshAccessToken(token: String): String?
    fun getUserByToken(jwt: String): UserDTO
    fun logout(jwt: String)
    fun verifyAccount(jwt: String): UserDTO
}