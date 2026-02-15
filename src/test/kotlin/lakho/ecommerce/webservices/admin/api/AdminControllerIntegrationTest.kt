package lakho.ecommerce.webservices.admin.api

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.auth.services.EmailService
import lakho.ecommerce.webservices.user.Roles
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
class AdminControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var emailService: EmailService

    private var adminToken: String = ""
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

        // Login as an existing admin user (from seed data)
        val adminLoginRequest = LoginRequest(
            email = "admin@ecommerce.com",
            password = "admin123"
        )

        val adminLoginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val adminAuthResponse = objectMapper.readTree(adminLoginResult.response.contentAsString)
        adminToken = adminAuthResponse.get("accessToken").asText()

        // Register and login as consumer
        val consumerRegisterRequest = RegisterRequest(
            email = "consumer@test.com",
            password = "P@ssword123",
            roles = setOf(Roles.CONSUMER)
        )

        val consumerLoginRequest = LoginRequest(
            email = "consumer@test.com",
            password = "P@ssword123"
        )


        var consumerResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consumerRegisterRequest))
        ).andReturn()

        if(consumerResult.response.status != 200) {
            consumerResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(consumerLoginRequest))
            ).andReturn()
        }

        val consumerAuthResponse = objectMapper.readTree(consumerResult.response.contentAsString)
        consumerToken = consumerAuthResponse.get("accessToken").asText()
    }

    @Test
    fun `listUsers should return paginated users for admin`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.totalPages").exists())
    }

    @Test
    fun `listUsers should return 401 without authentication`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `listUsers should return 403 for non-admin user`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $consumerToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `getUser should return user by ID for admin`() {
        // Arrange - First get list of users to get a valid ID
        val usersResult = mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val usersResponse = objectMapper.readTree(usersResult.response.contentAsString)
        val firstUserId = usersResponse.get("content").get(0).get("id").asText()

        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users/$firstUserId")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(firstUserId))
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.username").exists())
    }

    @Test
    fun `getUser should return 404 for non-existent user`() {
        // Arrange
        val nonExistentId = "00000000-0000-0000-0000-000000000000"

        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users/$nonExistentId")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getUser should return 403 for non-admin user`() {
        // Arrange
        val usersResult = mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val usersResponse = objectMapper.readTree(usersResult.response.contentAsString)
        val firstUserId = usersResponse.get("content").get(0).get("id").asText()

        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users/$firstUserId")
                .header("Authorization", "Bearer $consumerToken")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `listConsumers should return paginated consumers for admin`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/consumers")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `listConsumers should return 403 for non-admin user`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/consumers")
                .header("Authorization", "Bearer $consumerToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `listStores should return paginated stores for admin`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/stores")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `listStores should return 403 for non-admin user`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/stores")
                .header("Authorization", "Bearer $consumerToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `listUsers should handle pagination correctly`() {
        // Act & Assert - First page
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(1))

        // Act & Assert - Second page
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "1")
                .param("size", "1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.number").value(1))
    }

    @Test
    fun `listUsers should use default pagination parameters`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
    }
}
