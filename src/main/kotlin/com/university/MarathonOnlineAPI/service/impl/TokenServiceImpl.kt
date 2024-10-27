package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.config.JwtProperties
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.service.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenServiceImpl(
    jwtProperties: JwtProperties
): TokenService {

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray()
    )

    override fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any>
    ): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(secretKey)
            .compact()

    override fun extractEmail(token: String): String? {
        return try {
            getAllClaims(token).subject
        } catch (e: Exception) {
            throw AuthenticationException("Invalid token: ${e.message}")
        }
    }

    override fun isExpired(token: String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))

    override fun isValid(token: String, userDetails: UserDetails): Boolean {
        val email = extractEmail(token)
            ?: throw AuthenticationException("Token is invalid: email not found.")

        return userDetails.username == email && !isExpired(token)
    }

    private fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser()
            .setSigningKey(secretKey)
            .build()

        return parser
            .parseClaimsJws(token)
            .body
    }
}