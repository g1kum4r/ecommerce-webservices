package lakho.ecommerce.webservices.event.services

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.event.repositories.EventRepository
import lakho.ecommerce.webservices.event.repositories.entities.Event
import lakho.ecommerce.webservices.event.repositories.entities.EventStatus
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    companion object {
        private const val EVENT_CACHE_PREFIX = "event:"
        private const val EVENT_TYPE_PREFIX = "event:type:"
        private val CACHE_TTL = Duration.ofHours(24)
    }

    @Transactional
    fun createEvent(
        eventType: String,
        userId: UUID,
        email: String,
        metadata: Map<String, Any>? = null
    ): Event {
        val event = Event(
            eventType = eventType,
            userId = userId,
            email = email,
            metadata = metadata?.let { objectMapper.writeValueAsString(it) },
            status = EventStatus.PENDING
        )

        val savedEvent = eventRepository.save(event)
        cacheEvent(savedEvent)

        logger.info("Event created: eventId={}, eventType={}, userId={}", savedEvent.id, eventType, userId)
        return savedEvent
    }

    fun findById(id: UUID): Event? {
        val cacheKey = "$EVENT_CACHE_PREFIX$id"

        // Try to get from cache
        val cachedEvent = redisTemplate.opsForValue().get(cacheKey) as? Event
        if (cachedEvent != null) {
            logger.debug("Event found in cache: eventId={}", id)
            return cachedEvent
        }

        // Get from database
        val event = eventRepository.findById(id).orElse(null)
        if (event != null) {
            cacheEvent(event)
        }

        return event
    }

    fun findLatestByEventTypeAndUserId(eventType: String, userId: UUID): Event? {
        val cacheKey = "$EVENT_TYPE_PREFIX$eventType:$userId"

        // Try to get from cache
        val cachedEventId = redisTemplate.opsForValue().get(cacheKey) as? String
        if (cachedEventId != null) {
            val event = findById(UUID.fromString(cachedEventId))
            if (event != null) {
                logger.debug("Latest event found in cache: eventType={}, userId={}", eventType, userId)
                return event
            }
        }

        // Get from database
        val event = eventRepository.findLatestByEventTypeAndUserId(eventType, userId)
        if (event != null) {
            cacheEvent(event)
            redisTemplate.opsForValue().set(cacheKey, event.id.toString(), CACHE_TTL)
        }

        return event
    }

    @Transactional
    fun markAsProcessing(id: UUID): Event {
        val event = eventRepository.findById(id).orElseThrow {
            IllegalArgumentException("Event not found: $id")
        }

        val now = Instant.now()
        eventRepository.updateAttempt(
            id = id,
            status = EventStatus.PROCESSING.name,
            attemptCount = event.attemptCount + 1,
            lastAttemptAt = now,
            updatedAt = now
        )

        val updatedEvent = event.copy(
            status = EventStatus.PROCESSING,
            attemptCount = event.attemptCount + 1,
            lastAttemptAt = now,
            updatedAt = now
        )

        cacheEvent(updatedEvent)
        logger.info("Event marked as processing: eventId={}, attempt={}", id, updatedEvent.attemptCount)

        return updatedEvent
    }

    @Transactional
    fun markAsCompleted(id: UUID) {
        val event = eventRepository.findById(id).orElseThrow {
            IllegalArgumentException("Event not found: $id")
        }

        val now = Instant.now()
        eventRepository.markAsCompleted(
            id = id,
            status = EventStatus.COMPLETED.name,
            completedAt = now,
            updatedAt = now
        )

        logger.info("Event marked as completed: eventId={}, eventType={}", id, event.eventType)

        // Remove from cache after successful completion
        removeFromCache(id, event.eventType, event.userId)
    }

    @Transactional
    fun markAsFailed(id: UUID) {
        val event = eventRepository.findById(id).orElseThrow {
            IllegalArgumentException("Event not found: $id")
        }

        val now = Instant.now()
        eventRepository.updateAttempt(
            id = id,
            status = EventStatus.FAILED.name,
            attemptCount = event.attemptCount,
            lastAttemptAt = event.lastAttemptAt ?: now,
            updatedAt = now
        )

        val updatedEvent = event.copy(
            status = EventStatus.FAILED,
            updatedAt = now
        )

        cacheEvent(updatedEvent)
        logger.warn("Event marked as failed: eventId={}, eventType={}, attempts={}", id, event.eventType, event.attemptCount)
    }

    fun findPendingEvents(): List<Event> {
        return eventRepository.findByStatus(EventStatus.PENDING.name)
    }

    private fun cacheEvent(event: Event) {
        val cacheKey = "$EVENT_CACHE_PREFIX${event.id}"
        redisTemplate.opsForValue().set(cacheKey, event, CACHE_TTL)

        // Also cache by event type and user ID for quick lookup
        val typeCacheKey = "$EVENT_TYPE_PREFIX${event.eventType}:${event.userId}"
        redisTemplate.opsForValue().set(typeCacheKey, event.id.toString(), CACHE_TTL)
    }

    private fun removeFromCache(id: UUID, eventType: String, userId: UUID) {
        val cacheKey = "$EVENT_CACHE_PREFIX$id"
        val typeCacheKey = "$EVENT_TYPE_PREFIX$eventType:$userId"

        redisTemplate.delete(cacheKey)
        redisTemplate.delete(typeCacheKey)

        logger.debug("Event removed from cache: eventId={}", id)
    }
}
