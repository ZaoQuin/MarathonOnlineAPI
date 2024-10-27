package com.university.MarathonOnlineAPI.controller.auth

import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthenticationRequest): AuthenticationResponse {
        return try {
            authenticationService.authentication(authRequest)
        } catch (e: AuthenticationException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during authentication.")
        }
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody request: RefreshTokenRequest): TokenResponse {
        return try {
            authenticationService.refreshAccessToken(request.token)
                ?.mapToTokenResponse()
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token")
        } catch (e: AuthenticationException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during token refresh.")
        }
    }

    private fun String.mapToTokenResponse(): TokenResponse =
        TokenResponse(
            token = this
        )
}
