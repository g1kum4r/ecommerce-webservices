package lakho.ecommerce.webservices.common.services

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.common.event.events.BaseEvent
import lakho.ecommerce.webservices.common.repositories.EventRepository
import lakho.ecommerce.webservices.common.repositories.entities.Event
import lakho.ecommerce.webservices.common.repositories.entities.EventStatus
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Service for managing generic events across all categories.
 * Handles event persistence, caching, and status management.
 */
@Service
class EventService(
    private val eventRepository: EventRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    companion object {
        private const val EVENT_CACHE_PREFIX = "event:"
        private const val EVENT_CATEGORY_PREFIX = "event:category:"
        private const val EVENT_TYPE_PREFIX = "event:type:"
        private val CACHE_TTL = Duration.ofHours(24)
    }

    /**
     * Creates an event from a BaseEvent instance.
     * Automatically extracts category, type, and metadata from the event.
     * Combines userId and email into eventBody as JSONB.
     */
    @Transactional
    fun createEvent(baseEvent: BaseEvent, userId: UUID?, email: String?): Event {
        // Build event body with userId, email, and metadata
        val eventBodyMap = mutableMapOf<String, Any?>()
        if (userId != null) eventBodyMap["userId"] = userId.toString()
        if (email != null) eventBodyMap["email"] = email
        eventBodyMap.putAll(baseEvent.getMetadata())

        val event = Event(
            id = baseEvent.eventId,
            category = baseEvent.getEventCategory().name,
            type = baseEvent.getEventType(),
            body = objectMapper.valueToTree(eventBodyMap),
            metadata = null, // Can be used for additional tracking data if needed
            status = EventStatus.PENDING
        )

        val savedEvent = eventRepository.save(event)
        cacheEvent(savedEvent)

        logger.info(
            "Event created: eventId={}, category={}, type={}, userId={}",
            savedEvent.id,
            savedEvent.category,
            savedEvent.type,
            userId
        )
        return savedEvent
    }

    /**
     * Creates an event with explicit parameters.
     * Use this for custom event creation outside the BaseEvent hierarchy.
     */
    @Transactional
    fun createEvent(
        category: String,
        type: String,
        body: Map<String, Any>,
        metadata: Map<String, Any>? = null
    ): Event {
        val event = Event(
            category = category,
            type = type,
            body = objectMapper.valueToTree(body),
            metadata = metadata?.let { objectMapper.valueToTree(it) },
            status = EventStatus.PENDING
        )

        val savedEvent = eventRepository.save(event)
        cacheEvent(savedEvent)

        logger.info(
            "Event created: eventId={}, category={}, type={}",
            savedEvent.id,
            category,
            type
        )
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

    /**
     * Finds the latest event by event type and user ID.
     */
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

        // Get from database (pass userId as String for JSONB query)
        val event = eventRepository.findLatestByEventTypeAndUserId(eventType, userId.toString())
        if (event != null) {
            cacheEvent(event)
            redisTemplate.opsForValue().set(cacheKey, event.id.toString(), CACHE_TTL)
        }

        return event
    }

    /**
     * Finds events by category and status.
     */
    fun findByEventCategoryAndStatus(eventCategory: String, status: EventStatus): List<Event> {
        return eventRepository.findByEventCategoryAndStatus(eventCategory, status.name)
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

        logger.info("Event marked as completed: eventId={}, type={}", id, event.type)

        // Remove from cache after successful completion
        val userId = event.body?.get("userId")?.asText()?.let { UUID.fromString(it) }
        removeFromCache(id, event.category, event.type, userId)
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
        logger.warn("Event marked as failed: eventId={}, type={}, attempts={}", id, event.type, event.attemptCount)
    }

    fun findPendingEvents(): List<Event> {
        return eventRepository.findByStatus(EventStatus.PENDING.name)
    }

    private fun cacheEvent(event: Event) {
        val cacheKey = "$EVENT_CACHE_PREFIX${event.id}"
        redisTemplate.opsForValue().set(cacheKey, event, CACHE_TTL)

        // Cache by event category
        val categoryCacheKey = "$EVENT_CATEGORY_PREFIX${event.category}"
        redisTemplate.opsForSet().add(categoryCacheKey, event.id.toString())
        redisTemplate.expire(categoryCacheKey, CACHE_TTL)

        // Cache by event type and user ID for quick lookup (extract userId from body)
        val userId = event.body?.get("userId")?.asText()?.let { UUID.fromString(it) }
        if (userId != null) {
            val typeCacheKey = "$EVENT_TYPE_PREFIX${event.type}:$userId"
            redisTemplate.opsForValue().set(typeCacheKey, event.id.toString(), CACHE_TTL)
        }
    }

    private fun removeFromCache(id: UUID, eventCategory: String, eventType: String, userId: UUID?) {
        val cacheKey = "$EVENT_CACHE_PREFIX$id"
        redisTemplate.delete(cacheKey)

        // Remove from category set
        val categoryCacheKey = "$EVENT_CATEGORY_PREFIX$eventCategory"
        redisTemplate.opsForSet().remove(categoryCacheKey, id.toString())

        // Remove type cache if user ID exists
        if (userId != null) {
            val typeCacheKey = "$EVENT_TYPE_PREFIX$eventType:$userId"
            redisTemplate.delete(typeCacheKey)
        }

        logger.debug("Event removed from cache: eventId={}, category={}", id, eventCategory)
    }
}