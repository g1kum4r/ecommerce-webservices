package lakho.ecommerce.webservices.storeowner.api

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.common.services.EmailService
import lakho.ecommerce.webservices.common.enums.Roles
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class StoreOwnerControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var emailService: EmailService

    private var storeToken: String = ""
    private var consumerToken: String = ""

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

    @BeforeEach
    fun setup() {
        // Mock email service to prevent actual email sending during tests
        doNothing().whenever(emailService).sendVerificationEmail(any(), any(), any())
        doNothing().whenever(emailService).sendPasswordResetEmail(any(), any(), any())
        doNothing().whenever(emailService).sendPasswordResetConfirmationEmail(any(), any())

        // Register and login as store
        val storeRegisterRequest = RegisterRequest(
            email = "store@test.com",
            password = "P@ssw0rd123",
            roles = setOf(Roles.STORE_OWNER)
        )

        val storeLoginRequest = LoginRequest(
            email = "store@test.com",
            password = "P@ssw0rd123",
        )

        var storeRegisterResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(storeRegisterRequest))
        )
            .andReturn()

        if (storeRegisterResult.response.status != 200) {
            storeRegisterResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(storeLoginRequest))
            )
                .andReturn()
        }

        val storeAuthResponse = objectMapper.readTree(storeRegisterResult.response.contentAsString)
        storeToken = storeAuthResponse.get("accessToken").asText()

        // Register and login as consumer
        val consumerRegisterRequest = RegisterRequest(
            email = "consumer@test.com",
            password = "P@ssw0rd123",
            roles = setOf(Roles.CONSUMER)
        )

        val consumerLoginRequest = LoginRequest(
            email = "consumer@test.com",
            password = "P@ssw0rd123"
        )

        var consumerRegisterResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consumerRegisterRequest))
        )
            .andReturn()

        if (consumerRegisterResult.response.status != 200) {
            consumerRegisterResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(consumerLoginRequest))
            ).andReturn()
        }

        val consumerAuthResponse = objectMapper.readTree(consumerRegisterResult.response.contentAsString)
        consumerToken = consumerAuthResponse.get("accessToken").asText()
    }

    @Test
    fun `getProfile should return store profile for authenticated store`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
                .header("Authorization", "Bearer $storeToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("store@test.com"))
            .andExpect(jsonPath("$.username").exists())
    }

    @Test
    fun `getProfile should return 401 without authentication`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `getProfile should return 403 for non-store user`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
                .header("Authorization", "Bearer $consumerToken")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `getProfile should return profile with correct role`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
                .header("Authorization", "Bearer $storeToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles[0]").value("STORE_OWNER"))
    }

    @Test
    fun `getProfile should return 401 with invalid token`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
                .header("Authorization", "Bearer invalid.token.here")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `getProfile should return 401 with expired token`() {
        // Arrange - Use a clearly expired token format
        val expiredToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.4Adcj0MqVKXb8JDKLngJh8fmLJ6bsjqjQaD7RbqY9x8"

        // Act & Assert
        mockMvc.perform(
            get("/api/storeowner/profile")
                .header("Authorization", "Bearer $expiredToken")
        )
            .andExpect(status().isUnauthorized)
    }
}
