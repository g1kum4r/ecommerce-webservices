package lakho.ecommerce.webservices.event.services

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.event.repositories.EventRepository
import lakho.ecommerce.webservices.event.repositories.entities.Event
import lakho.ecommerce.webservices.event.repositories.entities.EventStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventServiceTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Mock
    private lateinit var valueOperations: ValueOperations<String, Any>

    @Mock
    private lateinit var setOperations: SetOperations<String, Any>

    private lateinit var objectMapper: ObjectMapper
    private lateinit var eventService: EventService

    private val testUserId = UUID.randomUUID()
    private val testEventId = UUID.randomUUID()
    private val testEmail = "test@example.com"

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
        eventService = EventService(eventRepository, redisTemplate, objectMapper)

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(redisTemplate.opsForSet()).thenReturn(setOperations)
        whenever(redisTemplate.expire(any(), any<Duration>())).thenReturn(true)
    }

    @Test
    fun `createEvent should save event to database and cache`() {
        // Arrange
        val category = "APP_FLOW"
        val type = "USER_REGISTERED"
        val body = mapOf(
            "userId" to testUserId.toString(),
            "email" to testEmail,
            "username" to "testuser",
            "verificationToken" to "token123"
        )

        val savedEvent = Event(
            id = testEventId,
            category = category,
            type = type,
            body = objectMapper.valueToTree(body),
            status = EventStatus.PENDING
        )

        whenever(eventRepository.save(any<Event>())).thenReturn(savedEvent)

        // Act
        val result = eventService.createEvent(category, type, body)

        // Assert
        assertNotNull(result)
        assertEquals(testEventId, result.id)
        assertEquals(category, result.category)
        assertEquals(type, result.type)
        assertEquals(EventStatus.PENDING, result.status)

        verify(eventRepository).save(any<Event>())
        verify(valueOperations, times(2)).set(any(), any(), eq(Duration.ofHours(24)))
    }

    @Test
    fun `findById should return event from cache if available`() {
        // Arrange
        val cachedEvent = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = "USER_REGISTERED",
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PENDING
        )

        whenever(valueOperations.get("event:$testEventId")).thenReturn(cachedEvent)

        // Act
        val result = eventService.findById(testEventId)

        // Assert
        assertNotNull(result)
        assertEquals(testEventId, result?.id)
        verify(valueOperations).get("event:$testEventId")
        verify(eventRepository, never()).findById(any())
    }

    @Test
    fun `findById should fetch from database if not in cache`() {
        // Arrange
        val event = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = "USER_REGISTERED",
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PENDING
        )

        whenever(valueOperations.get("event:$testEventId")).thenReturn(null)
        whenever(eventRepository.findById(testEventId)).thenReturn(Optional.of(event))

        // Act
        val result = eventService.findById(testEventId)

        // Assert
        assertNotNull(result)
        assertEquals(testEventId, result?.id)
        verify(valueOperations).get("event:$testEventId")
        verify(eventRepository).findById(testEventId)
        verify(valueOperations, times(2)).set(any(), any(), eq(Duration.ofHours(24)))
    }

    @Test
    fun `markAsProcessing should update event status and increment attempt count`() {
        // Arrange
        val event = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = "USER_REGISTERED",
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PENDING,
            attemptCount = 0
        )

        whenever(eventRepository.findById(testEventId)).thenReturn(Optional.of(event))

        // Act
        val result = eventService.markAsProcessing(testEventId)

        // Assert
        assertEquals(EventStatus.PROCESSING, result.status)
        assertEquals(1, result.attemptCount)
        verify(eventRepository).updateAttempt(
            eq(testEventId),
            eq(EventStatus.PROCESSING.name),
            eq(1),
            any(),
            any()
        )
        verify(valueOperations, times(2)).set(any(), any(), eq(Duration.ofHours(24)))
    }

    @Test
    fun `markAsCompleted should update event and remove from cache`() {
        // Arrange
        val event = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = "USER_REGISTERED",
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PROCESSING
        )

        whenever(eventRepository.findById(testEventId)).thenReturn(Optional.of(event))

        // Act
        eventService.markAsCompleted(testEventId)

        // Assert
        verify(eventRepository).markAsCompleted(
            eq(testEventId),
            eq(EventStatus.COMPLETED.name),
            any(),
            any()
        )
        verify(redisTemplate).delete("event:$testEventId")
        verify(redisTemplate).delete("event:type:USER_REGISTERED:$testUserId")
    }

    @Test
    fun `markAsFailed should update event status to FAILED`() {
        // Arrange
        val event = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = "USER_REGISTERED",
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PROCESSING,
            attemptCount = 1
        )

        whenever(eventRepository.findById(testEventId)).thenReturn(Optional.of(event))

        // Act
        eventService.markAsFailed(testEventId)

        // Assert
        verify(eventRepository).updateAttempt(
            eq(testEventId),
            eq(EventStatus.FAILED.name),
            eq(1),
            any(),
            any()
        )
        verify(valueOperations, times(2)).set(any(), any(), eq(Duration.ofHours(24)))
    }

    @Test
    fun `findLatestByEventTypeAndUserId should return cached event if available`() {
        // Arrange
        val eventType = "USER_REGISTERED"
        val event = Event(
            id = testEventId,
            category = "APP_FLOW",
            type = eventType,
            body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
            status = EventStatus.PENDING
        )

        whenever(valueOperations.get("event:type:$eventType:$testUserId")).thenReturn(testEventId.toString())
        whenever(valueOperations.get("event:$testEventId")).thenReturn(event)

        // Act
        val result = eventService.findLatestByEventTypeAndUserId(eventType, testUserId)

        // Assert
        assertNotNull(result)
        assertEquals(testEventId, result?.id)
        verify(eventRepository, never()).findLatestByEventTypeAndUserId(any(), any())
    }

    @Test
    fun `findPendingEvents should return list of pending events`() {
        // Arrange
        val pendingEvents = listOf(
            Event(
                id = UUID.randomUUID(),
                category = "APP_FLOW",
                type = "USER_REGISTERED",
                body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
                status = EventStatus.PENDING
            ),
            Event(
                id = UUID.randomUUID(),
                category = "APP_FLOW",
                type = "PASSWORD_RESET",
                body = objectMapper.valueToTree(mapOf("userId" to testUserId.toString(), "email" to testEmail)),
                status = EventStatus.PENDING
            )
        )

        whenever(eventRepository.findByStatus(EventStatus.PENDING.name)).thenReturn(pendingEvents)

        // Act
        val result = eventService.findPendingEvents()

        // Assert
        assertNotNull(result)
        assertEquals(2, result.size)
        verify(eventRepository).findByStatus(EventStatus.PENDING.name)
    }
}
