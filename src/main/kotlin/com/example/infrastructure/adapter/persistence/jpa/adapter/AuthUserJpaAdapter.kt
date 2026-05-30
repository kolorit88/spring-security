package com.example.infrastructure.adapter.persistence.jpa.adapter

import com.example.domain.port.AuthUserRepositoryPort
import com.example.infrastructure.adapter.persistence.jpa.entity.Role
import com.example.infrastructure.adapter.persistence.jpa.entity.UserEntity
import com.example.infrastructure.adapter.persistence.jpa.repository.UserJpaRepository
import org.springframework.context.annotation.Profile
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository

@Repository
@Profile("db", "test")
class AuthUserJpaAdapter(
    private val userJpaRepository: UserJpaRepository
) : AuthUserRepositoryPort {

    override fun findByEmail(email: String): UserDetails? {
        return userJpaRepository.findByEmail(email)
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }

    override fun createUser(
        email: String,
        encodedPassword: String,
        firstName: String,
        lastName: String,
        role: String
    ): UserDetails {
        val userEntity = UserEntity(
            id = null,
            email = email,
            firstName = firstName,
            lastName = lastName,
            isActive = true,
            password = encodedPassword,
            role = Role.valueOf(role)
        )
        return userJpaRepository.save(userEntity)
    }
}