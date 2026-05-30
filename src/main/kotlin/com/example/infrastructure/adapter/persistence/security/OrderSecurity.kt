package com.example.infrastructure.adapter.persistence.security

import com.example.domain.port.OrderRepositoryPort
import com.example.domain.port.UserRepositoryPort
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component("orderSecurity")
class OrderSecurity(
    private val orderRepositoryPort: OrderRepositoryPort,
    private val userRepositoryPort: UserRepositoryPort
) {
    fun isOrderOwner(authentication: Authentication, orderId: Long): Boolean {
        val email = authentication.name
        val order = orderRepositoryPort.findById(orderId) ?: return false
        val user = userRepositoryPort.findByEmail(email) ?: return false
        return order.userId == user.id
    }
}