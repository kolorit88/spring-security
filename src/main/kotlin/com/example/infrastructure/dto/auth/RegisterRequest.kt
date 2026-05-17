package com.example.infrastructure.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email не может быть пустым")
    @field:Email(message = "Некорректный формат email")
    val email: String,

    @field:NotBlank(message = "Пароль не может быть пустым")
    @field:Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    val password: String,

    @field:NotBlank(message = "Имя не может быть пустым")
    val firstName: String,

    @field:NotBlank(message = "Фамилия не может быть пустой")
    val lastName: String
)