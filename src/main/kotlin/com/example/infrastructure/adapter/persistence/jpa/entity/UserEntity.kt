package com.example.infrastructure.adapter.persistence.jpa.entity

import com.example.domain.model.User
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    private val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orders: MutableList<OrderEntity> = mutableListOf()
) : UserDetails {

    // Конструктор без параметров для JPA
    constructor() : this(
        id = null,
        email = "",
        firstName = "",
        lastName = "",
        isActive = true,
        password = "",
        role = Role.USER
    )

    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            isActive = isActive
        )
    }

    companion object {
        fun fromDomain(
            user: User,
            password: String = "",
            role: Role = Role.USER
        ): UserEntity {
            return UserEntity(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                isActive = user.isActive,
                password = password,
                role = role
            )
        }
    }

    // Реализация UserDetails
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isActive
}

enum class Role {
    USER, ADMIN
}