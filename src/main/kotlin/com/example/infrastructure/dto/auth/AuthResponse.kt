package com.example.infrastructure.dto.auth

data class AuthResponse(
    val token: String,
    val email: String,
    val role: String
)