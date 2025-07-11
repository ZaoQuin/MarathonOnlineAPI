package com.university.MarathonOnlineAPI.controller.auth

import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.UserException
import com.university.MarathonOnlineAPI.service.AuthenticationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["http://localhost:3000/"])
class AuthController(
    private val authenticationService: AuthenticationService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @GetMapping
    fun getMe(@RequestHeader("Authorization") token: String): ResponseEntity<UserDTO> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val foundUser = authenticationService.getUserByToken(jwt)
            ResponseEntity(foundUser, HttpStatus.OK)
        } catch (e: UserException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PatchMapping
    fun verifyAccount(@RequestHeader("Authorization") token: String): ResponseEntity<UserDTO> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val foundUser = authenticationService.verifyAccount(jwt)
            ResponseEntity(foundUser, HttpStatus.OK)
        } catch (e: AuthenticationException) {
            logger.error("Authentication failed: ${e.message}", e)
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        } catch (e: Exception) {
            logger.error("Error while verifying account: ${e.message}", e)
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse> {
        return try {
            val response = authenticationService.authentication(authRequest)
            ResponseEntity.ok(response)
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        return try {
            val token = authenticationService.refreshAccessToken(request.token)
                ?.mapToTokenResponse()

            token?.let {
                ResponseEntity.ok(it)
            } ?: run {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        return try {
            val jwt = token.replace("Bearer ", "")
            authenticationService.logout(jwt)
            ResponseEntity.ok("Successfully logged out")
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: ${e.message}")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to logout due to unexpected error: ${e.message}")
        }
    }

    @DeleteMapping("")
    fun deleteAccount(@RequestHeader("Authorization") token: String): ResponseEntity<UserDTO> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val userDTO = authenticationService.deleteAccount(jwt)
            ResponseEntity.ok(userDTO)
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun String.mapToTokenResponse(): TokenResponse =
        TokenResponse(
            token = this
        )
}
