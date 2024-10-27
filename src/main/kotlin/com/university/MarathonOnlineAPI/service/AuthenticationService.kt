package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.controller.auth.AuthenticationRequest
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationResponse

interface AuthenticationService {
    fun authentication(authRequest: AuthenticationRequest): AuthenticationResponse
    fun refreshAccessToken(token: String): String?
}