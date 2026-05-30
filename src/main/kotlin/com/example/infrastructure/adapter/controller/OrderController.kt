package com.example.infrastructure.adapter.controller

import jakarta.validation.Valid
import com.example.domain.model.OrderStatus
import com.example.domain.service.OrderService
import com.example.infrastructure.adapter.persistence.jpa.entity.UserEntity
import com.example.infrastructure.dto.requests.order.OrderCreateRequest
import com.example.infrastructure.dto.requests.order.OrderStatusUpdateRequest
import com.example.infrastructure.dto.response.restaurant.OrderResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.example.shared.utils.mapper.OrderMapper
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    fun listOrders(
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) status: String?,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<List<OrderResponse>> {
        val orderStatus = status?.let { OrderStatus.valueOf(it.uppercase()) }

        val effectiveUserId = if (principal.authorities.any { it.authority == "ROLE_ADMIN" }) {
            userId  // ADMIN может фильтровать по любому userId
        } else {
            // USER видит только свои заказы
            (principal as? UserEntity)?.id ?: throw IllegalStateException("User id not found")
        }

        val orders = orderService.getAllOrders(effectiveUserId, orderStatus)
        val response = orders.map { orderMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun createOrder(@Valid @RequestBody createRequest: OrderCreateRequest): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(createRequest.userId, createRequest.dishIds)
        val response = orderMapper.toResponse(order)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(authentication, #id)")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderById(id)
        val response = orderMapper.toResponse(order)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestBody statusUpdateRequest: OrderStatusUpdateRequest
    ): ResponseEntity<OrderResponse> {
        val newStatus = OrderStatus.valueOf(statusUpdateRequest.status.uppercase())
        val updatedOrder = orderService.updateOrderStatus(id, newStatus)
        val response = orderMapper.toResponse(updatedOrder)
        return ResponseEntity.ok(response)
    }
}