package com.example.infrastructure.adapter.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SecurityIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        @Suppress("unused")
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("restaurant-test-db")
            withUsername("test")
            withPassword("test")
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `access to public GET endpoints without token returns 200`() {
        mockMvc.perform(get("/api/v1/restaurants"))
            .andExpect(status().isOk)
    }

    @Test
    fun `access to admin POST endpoint without token returns 401`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Test","address":"Addr"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `access to admin POST endpoint with USER role returns 403`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Test","address":"Addr"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `access to admin POST endpoint with ADMIN role returns 201`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"SecurityTest Restaurant","address":"Addr"}""")
        ).andExpect(status().isCreated)
    }
}
