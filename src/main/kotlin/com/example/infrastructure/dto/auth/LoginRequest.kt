package com.example.infrastructure.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email не может быть пустым")
    val email: String,

    @field:NotBlank(message = "Пароль не может быть пустым")
    val password: String
)