package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.config.JwtProperties
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationRequest
import com.university.MarathonOnlineAPI.controller.auth.AuthenticationResponse
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.ERole
import com.university.MarathonOnlineAPI.entity.EUserStatus
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.service.AuthenticationService
import com.university.MarathonOnlineAPI.service.RefreshTokenService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
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
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService
) : AuthenticationService {


    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

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

            val userDTO = refreshTokenService.save(refreshToken, user)

            AuthenticationResponse(
                fullName = userDTO.fullName?:"",
                email = userDTO.email?:"",
                accessToken = accessToken,
                refreshToken = refreshToken,
                role = userDTO.role?:ERole.RUNNER,
                status = userDTO.status?: EUserStatus.PENDING,
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

    override fun getUserByToken(jwt: String): UserDTO {
        return if (tokenService.validateToken(jwt)) {
            tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")
        } else {
            throw AuthenticationException("Invalid or expired token")
        }
    }

    override fun logout(jwt: String) {
        try {
                val email = tokenService.extractEmail(jwt) ?: throw AuthenticationException("Invalid token")

            if (!userService.removeRefreshTokenByEmail(email)) {
                throw AuthenticationException("Failed to remove refresh token")
            }

            tokenService.invalidateToken(jwt)

        } catch (e: AuthenticationException) {
            throw AuthenticationException("Logout failed: ${e.message}")
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred during logout: ${e.message}", e)
        }
    }

    override fun verifyAccount(jwt: String): UserDTO {
        return try {
            val user = getUserByToken(jwt)
            user.status = EUserStatus.PUBLIC
            userService.updateUser(user)
        } catch (e: AuthenticationException) {
            logger.error("Authentication failed: ${e.message}", e)
            throw AuthenticationException("Authentication failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error while verifying account: ${e.message}", e)
            throw RuntimeException("Error while verifying account: ${e.message}", e)
        }
    }

    override fun deleteAccount(jwt: String): UserDTO {
        return if (tokenService.validateToken(jwt)) {
            tokenService.extractEmail(jwt)?.let { email ->
                val userDTO = userService.findByEmail(email)
                userDTO.status = EUserStatus.DELETED
                userService.updateUser(userDTO)
            } ?: throw AuthenticationException("Email not found in the token")

        } else {
            throw AuthenticationException("Invalid or expired token")
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
