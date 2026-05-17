package com.example.application.service

import com.example.domain.port.AuthUserRepositoryPort
import com.example.infrastructure.dto.auth.LoginRequest
import com.example.infrastructure.dto.auth.RegisterRequest
import com.example.infrastructure.dto.auth.AuthResponse
import com.example.shared.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authUserRepositoryPort: AuthUserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun register(request: RegisterRequest): AuthResponse {
        if (authUserRepositoryPort.existsByEmail(request.email)) {
            throw BusinessException.EmailAlreadyExists(request.email)
        }

        val hashedPassword = passwordEncoder.encode(request.password)
        val userDetails = authUserRepositoryPort.createUser(
            email = request.email,
            encodedPassword = hashedPassword,
            firstName = request.firstName,
            lastName = request.lastName,
            role = "USER"
        )

        val token = jwtService.generateToken(userDetails.username, "USER")
        log.info("Registered user: ${userDetails.username}")

        return AuthResponse(token, userDetails.username, "USER")
    }

    fun login(request: LoginRequest): AuthResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )
        } catch (e: BadCredentialsException) {
            throw BusinessException.InvalidCredentialsException()
        }

        val userDetails = authUserRepositoryPort.findByEmail(request.email)
            ?: throw BusinessException.UserNotFoundByEmail(request.email)

        val role = (userDetails.authorities.firstOrNull()?.authority?.removePrefix("ROLE_") ?: "USER")
        val token = jwtService.generateToken(userDetails.username, role)
        log.info("User logged in: ${userDetails.username}")

        return AuthResponse(token, userDetails.username, role)
    }
}