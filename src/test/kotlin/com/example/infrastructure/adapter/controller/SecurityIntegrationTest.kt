import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `access to public endpoints without token`() {
        mockMvc.perform(get("/api/v1/restaurants"))
            .andExpect(status().isOk)
    }

    @Test
    fun `access to admin endpoint without token returns 401`() {
        mockMvc.perform(post("/api/v1/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Test","address":"Addr"}"""))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `access to admin endpoint with user role returns 403`() {
        mockMvc.perform(post("/api/v1/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Test","address":"Addr"}"""))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `access to admin endpoint with admin role returns 201`() {
        mockMvc.perform(post("/api/v1/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Test","address":"Addr"}"""))
            .andExpect(status().isCreated)
    }
}