package controller

import io.restassured.http.ContentType
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.mockmvc.kotlin.extensions.Given
import io.restassured.module.mockmvc.kotlin.extensions.Then
import io.restassured.module.mockmvc.kotlin.extensions.When
import org.example.example.App
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [App::class])
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RestaurantControllerIntegrationTest {

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
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc)
    }

    // POST /api/v1/restaurants
    @Test
    @DisplayName("POST: Positive - Creates restaurant successfully with status 201")
    fun createRestaurant_ValidRequest_ReturnsCreated() {
        Given {
            contentType(ContentType.JSON)
            body("""{"name": "Italian Bistro", "address": "123 Main Street", "phone": "+1234567890", "email": "contact@italianbistro.com", "cuisineType": "Italian", "openingHours": "Mon-Sun: 10:00-22:00"}""")
        } When {
            post("/api/v1/restaurants")
        } Then {
            statusCode(201)
            body("id", notNullValue())
            body("name", equalTo("Italian Bistro"))
            body("address", equalTo("123 Main Street"))
            body("phone", equalTo("+1234567890"))
            body("email", equalTo("contact@italianbistro.com"))
            body("cuisineType", equalTo("Italian"))
            body("openingHours", equalTo("Mon-Sun: 10:00-22:00"))
        }
    }

    @Test
    @DisplayName("POST: Negative - Returns 400 when restaurant name is empty")
    fun createRestaurant_EmptyName_ReturnsBadRequest() {
        Given {
            contentType(ContentType.JSON)
            body("""{"name": "", "address": "123 Main Street", "phone": "+1234567890", "email": "test@test.com", "cuisineType": "Italian", "openingHours": "10:00-22:00"}""")
        } When {
            post("/api/v1/restaurants")
        } Then {
            statusCode(400)
            body("message", notNullValue())
        }
    }

    @Test
    @DisplayName("POST: Negative - Returns 400 when email format is invalid")
    fun createRestaurant_InvalidEmail_ReturnsBadRequest() {
        Given {
            contentType(ContentType.JSON)
            body("""{"name": "Valid Name", "address": "123 Main Street", "phone": "+1234567890", "email": "invalid-email", "cuisineType": "Italian", "openingHours": "10:00-22:00"}""")
        } When {
            post("/api/v1/restaurants")
        } Then {
            statusCode(400)
            body("message", notNullValue())
        }
    }

    // GET /api/v1/restaurants
    @Test
    @DisplayName("GET: Positive - Returns list of all restaurants with status 200")
    fun getAllRestaurants_ReturnsListOfRestaurants() {
        // Create two restaurants first
        RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Sushi House", "address": "5 Ocean Drive", "phone": "+1111111111", "email": "sushi@house.com", "cuisineType": "Japanese", "openingHours": "12:00-23:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)

        RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Pizza Palace", "address": "10 Pizza Lane", "phone": "+2222222222", "email": "info@pizzapalace.com", "cuisineType": "Italian", "openingHours": "11:00-23:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)

        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants")
        } Then {
            statusCode(200)
            body("size()", greaterThanOrEqualTo(2))
            body("[0].name", anyOf(equalTo("Sushi House"), equalTo("Pizza Palace")))
            body("[1].name", anyOf(equalTo("Sushi House"), equalTo("Pizza Palace")))
        }
    }

    // GET /api/v1/restaurants/{id}
    @Test
    @DisplayName("GET {id}: Positive - Returns restaurant by id with status 200")
    fun getRestaurantById_ExistingId_ReturnsRestaurant() {
        val id = RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Burger King", "address": "77 Fast Food Ave", "phone": "+3333333333", "email": "burger@king.com", "cuisineType": "American", "openingHours": "10:00-00:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)
            .extract()
            .path<Int>("id")

        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants/$id")
        } Then {
            statusCode(200)
            body("id", equalTo(id))
            body("name", equalTo("Burger King"))
            body("address", equalTo("77 Fast Food Ave"))
            body("cuisineType", equalTo("American"))
        }
    }

    @Test
    @DisplayName("GET {id}: Negative - Returns 404 when restaurant not found")
    fun getRestaurantById_NonExistentId_ReturnsNotFound() {
        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants/999999")
        } Then {
            statusCode(404)
            body("status", equalTo(404))
        }
    }

    // PUT /api/v1/restaurants/{id}
    @Test
    @DisplayName("PUT: Positive - Updates restaurant successfully with status 200")
    fun updateRestaurant_ValidRequest_ReturnsUpdatedRestaurant() {
        val id = RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Old Restaurant", "address": "Old Address", "phone": "+1111111111", "email": "old@restaurant.com", "cuisineType": "Local", "openingHours": "09:00-18:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)
            .extract()
            .path<Int>("id")

        Given {
            contentType(ContentType.JSON)
            body("""{"name": "New Restaurant Name", "address": "New Updated Address", "phone": "+9999999999", "email": "new@restaurant.com", "cuisineType": "International", "openingHours": "10:00-22:00"}""")
        } When {
            put("/api/v1/restaurants/$id")
        } Then {
            statusCode(200)
            body("id", equalTo(id))
            body("name", equalTo("New Restaurant Name"))
            body("address", equalTo("New Updated Address"))
            body("phone", equalTo("+9999999999"))
            body("email", equalTo("new@restaurant.com"))
            body("cuisineType", equalTo("International"))
            body("openingHours", equalTo("10:00-22:00"))
        }
    }

    @Test
    @DisplayName("PUT: Negative - Returns 404 when updating non-existent restaurant")
    fun updateRestaurant_NonExistentId_ReturnsNotFound() {
        Given {
            contentType(ContentType.JSON)
            body("""{"name": "Any Name", "address": "Any Address", "phone": "+1234567890", "email": "any@test.com", "cuisineType": "Any", "openingHours": "10:00-22:00"}""")
        } When {
            put("/api/v1/restaurants/999999")
        } Then {
            statusCode(404)
        }
    }

    // DELETE /api/v1/restaurants/{id}
    @Test
    @DisplayName("DELETE: Positive - Deletes restaurant successfully with status 204 and verifies it's gone")
    fun deleteRestaurant_ExistingId_ReturnsNoContent() {
        val id = RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Restaurant To Delete", "address": "Delete Me Street", "phone": "+4444444444", "email": "delete@me.com", "cuisineType": "Test", "openingHours": "10:00-20:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)
            .extract()
            .path<Int>("id")

        Given {
            accept(ContentType.JSON)
        } When {
            delete("/api/v1/restaurants/$id")
        } Then {
            statusCode(204)
        }

        // Verify restaurant no longer exists
        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants/$id")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @DisplayName("DELETE: Negative - Returns 404 when deleting non-existent restaurant")
    fun deleteRestaurant_NonExistentId_ReturnsNotFound() {
        Given {
            accept(ContentType.JSON)
        } When {
            delete("/api/v1/restaurants/999999")
        } Then {
            statusCode(404)
        }
    }

    // GET /api/v1/restaurants/{id}/dishes
    @Test
    @DisplayName("GET {id}/dishes: Positive - Returns empty list when restaurant has no dishes")
    fun getRestaurantDishes_NoDishes_ReturnsEmptyList() {
        val id = RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body("""{"name": "Empty Dishes Restaurant", "address": "No Food Street", "phone": "+5555555555", "email": "empty@dishes.com", "cuisineType": "Test", "openingHours": "09:00-21:00"}""")
            .post("/api/v1/restaurants")
            .then()
            .statusCode(201)
            .extract()
            .path<Int>("id")

        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants/$id/dishes")
        } Then {
            statusCode(200)
            body("size()", equalTo(0))
        }
    }

    @Test
    @DisplayName("GET {id}/dishes: Negative - Returns 404 when restaurant not found")
    fun getRestaurantDishes_NonExistentRestaurant_ReturnsNotFound() {
        Given {
            accept(ContentType.JSON)
        } When {
            get("/api/v1/restaurants/999999/dishes")
        } Then {
            statusCode(404)
        }
    }
}