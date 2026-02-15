package lakho.ecommerce.webservices.auth.api

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.user.Roles
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    @Test
    fun `register should create new consumer user successfully`() {
        // Arrange
        val request = RegisterRequest(
            email = "newuser@example.com",
            password = "password123",
            roles = setOf(Roles.CONSUMER)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `register should create new store user successfully`() {
        // Arrange
        val request = RegisterRequest(
            email = "newstore@example.com",
            password = "password123",
            roles = setOf(Roles.STORE)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `register should fail when trying to register as ADMIN`() {
        // Arrange
        val request = RegisterRequest(
            email = "admin@example.com",
            password = "password123",
            roles = setOf(Roles.ADMIN)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `register should fail when email already exists`() {
        // Arrange
        val email = "duplicate@example.com"
        val request1 = RegisterRequest(
            email = email,
            password = "password123",
            roles = setOf(Roles.CONSUMER)
        )
        val request2 = RegisterRequest(
            email = email,
            password = "password456",
            roles = setOf(Roles.CONSUMER)
        )

        // First registration
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        )
            .andExpect(status().isOk)

        // Act & Assert - Second registration with same email
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `register should fail with invalid email`() {
        // Arrange
        val request = RegisterRequest(
            email = "invalidemail",
            password = "password123",
            roles = setOf(Roles.CONSUMER)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `register should fail with short password`() {
        // Arrange
        val request = RegisterRequest(
            email = "user@example.com",
            password = "short",
            roles = setOf(Roles.CONSUMER)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `login should authenticate user successfully`() {
        // Arrange - Register user first
        val registerRequest = RegisterRequest(
            email = "login@example.com",
            password = "password123",
            roles = setOf(Roles.CONSUMER)
        )
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )

        val loginRequest = LoginRequest(
            email = "login@example.com",
            password = "password123"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `login should fail with invalid credentials`() {
        // Arrange
        val loginRequest = LoginRequest(
            email = "nonexistent@example.com",
            password = "wrongpassword"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `login should fail with wrong password`() {
        // Arrange - Register user first
        val registerRequest = RegisterRequest(
            email = "wrongpass@example.com",
            password = "correctpassword",
            roles = setOf(Roles.CONSUMER)
        )
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )

        val loginRequest = LoginRequest(
            email = "wrongpass@example.com",
            password = "wrongpassword"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `refresh should generate new tokens successfully`() {
        // Arrange - Register and get tokens
        val registerRequest = RegisterRequest(
            email = "refresh@example.com",
            password = "password123",
            roles = setOf(Roles.CONSUMER)
        )
        val registerResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val authResponse = objectMapper.readTree(registerResult.response.contentAsString)
        val refreshToken = authResponse.get("refreshToken").asText()

        val refreshRequest = RefreshRequest(refreshToken = refreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `refresh should fail with invalid token`() {
        // Arrange
        val refreshRequest = RefreshRequest(refreshToken = "invalid.token.here")

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `register should handle multiple roles`() {
        // Arrange
        val request = RegisterRequest(
            email = "multirole@example.com",
            password = "password123",
            roles = setOf(Roles.CONSUMER, Roles.STORE)
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }
}
