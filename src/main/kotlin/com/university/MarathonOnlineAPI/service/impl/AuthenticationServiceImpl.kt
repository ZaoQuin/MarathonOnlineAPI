package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.config.JwtProperties
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationRequest
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationResponse
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.service.AuthenticationService
import com.university.MarathonOnlineAPI.service.RefreshTokenService
import com.university.MarathonOnlineAPI.service.TokenService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationServiceImpl(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val refreshTokenService: RefreshTokenService
) : AuthenticationService {

    override fun authentication(authRequest: AuthenticationRequest): AuthenticationResponse {
        return try {
            authManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    authRequest.email,
                    authRequest.password
                )
            )

            val user = userDetailsService.loadUserByUsername(authRequest.email)

            val accessToken = generateAccessToken(user)
            val refreshToken = generateRefreshToken(user)

            refreshTokenService.save(refreshToken, user)

            AuthenticationResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } catch (e: Exception) {
            throw AuthenticationException("Authentication failed: ${e.message}")
        }
    }

    override fun refreshAccessToken(token: String): String? {
        val extractedEmail = tokenService.extractEmail(token)

        return extractedEmail?.let { email ->
            val currentUserDetails = userDetailsService.loadUserByUsername(email)
            val refreshTokenUserDetails = refreshTokenService.findUserDetailsByToken(token)

            if (!tokenService.isExpired(token) && currentUserDetails.username == refreshTokenUserDetails?.username) {
                generateAccessToken(currentUserDetails)
            } else {
                throw AuthenticationException("Invalid refresh token")
            }
        }
    }

    private fun generateRefreshToken(user: UserDetails) = tokenService.generate(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
    )

    private fun generateAccessToken(user: UserDetails) = tokenService.generate(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
    )
}
