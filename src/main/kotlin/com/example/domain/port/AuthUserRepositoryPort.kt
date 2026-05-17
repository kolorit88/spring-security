package com.example.domain.port

import org.springframework.security.core.userdetails.UserDetails

interface AuthUserRepositoryPort {
    fun findByEmail(email: String): UserDetails?
    fun existsByEmail(email: String): Boolean
    fun createUser(email: String, encodedPassword: String, firstName: String, lastName: String, role: String): UserDetails
}