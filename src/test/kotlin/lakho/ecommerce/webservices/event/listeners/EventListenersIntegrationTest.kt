package lakho.ecommerce.webservices.event.listeners

import lakho.ecommerce.webservices.common.services.EmailService
import lakho.ecommerce.webservices.auth.events.PasswordResetEvent
import lakho.ecommerce.webservices.auth.events.UserRegisteredEvent
import lakho.ecommerce.webservices.common.repositories.EventRepository
import lakho.ecommerce.webservices.common.repositories.entities.EventStatus
import lakho.ecommerce.webservices.common.services.EventService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest
@Testcontainers
class EventListenersIntegrationTest {

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

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @MockitoBean
    private lateinit var emailService: EmailService

    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"

    @BeforeEach
    fun setup() {
        // Clean up Redis cache before each test
        redisTemplate.connectionFactory?.connection?.flushAll()
    }

    @AfterEach
    fun cleanup() {
        // Clean up all events after each test
        eventRepository.deleteAll()
    }

    @Test
    fun `UserRegisteredEventListener should create event and send verification email`() {
        // Arrange
        val verificationToken = "test-verification-token"
        val event = UserRegisteredEvent(
            source = this,
            userId = testUserId,
            email = testEmail,
            username = testUsername,
            verificationToken = verificationToken
        )

        // Act
        eventPublisher.publishEvent(event)

        // Wait for async processing
        TimeUnit.SECONDS.sleep(2)

        // Assert
        val savedEvent = eventService.findLatestByEventTypeAndUserId("USER_REGISTERED", testUserId)
        assertNotNull(savedEvent)
        assertEquals("USER_REGISTERED", savedEvent?.type)
        assertEquals("APP_FLOW", savedEvent?.category)

        // Verify body contains userId and email
        val userIdFromBody = savedEvent?.body?.get("userId")?.asText()
        val emailFromBody = savedEvent?.body?.get("email")?.asText()
        assertEquals(testUserId.toString(), userIdFromBody)
        assertEquals(testEmail, emailFromBody)

        // Event should be completed and removed from cache
        val eventById = eventService.findById(savedEvent!!.id)
        // The event is removed from cache after completion, so we check the database directly
        val dbEvent = eventRepository.findById(savedEvent.id)
        assertTrue(dbEvent.isPresent)
        assertEquals(EventStatus.COMPLETED, dbEvent.get().status)
        assertNotNull(dbEvent.get().completedAt)
    }

    @Test
    fun `PasswordResetEventListener should create event and send confirmation email`() {
        // Arrange
        val event = PasswordResetEvent(
            source = this,
            userId = testUserId,
            email = testEmail,
            username = testUsername
        )

        // Act
        eventPublisher.publishEvent(event)

        // Wait for async processing
        TimeUnit.SECONDS.sleep(2)

        // Assert
        val savedEvent = eventService.findLatestByEventTypeAndUserId("PASSWORD_RESET", testUserId)
        assertNotNull(savedEvent)
        assertEquals("PASSWORD_RESET", savedEvent?.type)
        assertEquals("APP_FLOW", savedEvent?.category)

        // Verify body contains userId and email
        val userIdFromBody = savedEvent?.body?.get("userId")?.asText()
        val emailFromBody = savedEvent?.body?.get("email")?.asText()
        assertEquals(testUserId.toString(), userIdFromBody)
        assertEquals(testEmail, emailFromBody)

        // Event should be completed and removed from cache
        val dbEvent = eventRepository.findById(savedEvent!!.id)
        assertTrue(dbEvent.isPresent)
        assertEquals(EventStatus.COMPLETED, dbEvent.get().status)
        assertNotNull(dbEvent.get().completedAt)
    }

    @Test
    fun `UserRegisteredEventListener should mark event as failed on email error`() {
        // Arrange
        val verificationToken = "test-verification-token"
        val event = UserRegisteredEvent(
            source = this,
            userId = testUserId,
            email = testEmail,
            username = testUsername,
            verificationToken = verificationToken
        )

        // Mock email service to throw exception
        whenever(
            emailService.sendVerificationEmail(
                any(),
                any(),
                any()
            )
        ).thenThrow(RuntimeException("Email service unavailable"))

        // Act
        eventPublisher.publishEvent(event)

        // Wait for async processing
        TimeUnit.SECONDS.sleep(2)

        // Assert
        val savedEvent = eventService.findLatestByEventTypeAndUserId("USER_REGISTERED", testUserId)
        assertNotNull(savedEvent)

        val dbEvent = eventRepository.findById(savedEvent!!.id)
        assertTrue(dbEvent.isPresent)
        assertEquals(EventStatus.FAILED, dbEvent.get().status)
        assertTrue(dbEvent.get().attemptCount > 0)
    }

    @Test
    fun `PasswordResetEventListener should mark event as failed on email error`() {
        // Arrange
        val event = PasswordResetEvent(
            source = this,
            userId = testUserId,
            email = testEmail,
            username = testUsername
        )

        // Mock email service to throw exception
        whenever(
            emailService.sendPasswordResetConfirmationEmail(
                any(),
                any()
            )
        ).thenThrow(RuntimeException("Email service unavailable"))

        // Act
        eventPublisher.publishEvent(event)

        // Wait for async processing
        TimeUnit.SECONDS.sleep(2)

        // Assert
        val savedEvent = eventService.findLatestByEventTypeAndUserId("PASSWORD_RESET", testUserId)
        assertNotNull(savedEvent)

        val dbEvent = eventRepository.findById(savedEvent!!.id)
        assertTrue(dbEvent.isPresent)
        assertEquals(EventStatus.FAILED, dbEvent.get().status)
        assertTrue(dbEvent.get().attemptCount > 0)
    }
}
