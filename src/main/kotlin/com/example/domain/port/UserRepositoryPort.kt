package com.example.domain.port

import com.example.domain.model.User
import com.example.infrastructure.adapter.persistence.jpa.entity.Role
import com.example.infrastructure.adapter.persistence.jpa.entity.UserEntity

interface UserRepositoryPort : BaseRepositoryPort<User> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}