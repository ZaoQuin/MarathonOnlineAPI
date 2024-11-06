package com.university.MarathonOnlineAPI.service

import org.springframework.security.core.userdetails.UserDetails
import java.util.*

interface TokenService {
    fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String

    fun extractEmail(token: String): String?
    fun isExpired(token: String): Boolean
    fun isValid(token: String, userDetails: UserDetails): Boolean
    fun validateToken(token: String): Boolean
    fun invalidateToken(token: String)
}