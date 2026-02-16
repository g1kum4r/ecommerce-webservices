package lakho.ecommerce.webservices.common.repositories.entities

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * Event entity representing a persisted event in the database.
 * Supports generic event types across different categories.
 *
 * Uses JSONB for efficient JSON storage and querying in PostgreSQL.
 */
@Table("events")
data class Event(
    @Id val id: UUID = UUID.randomUUID(),

    /**
     * Event category (APP_FLOW, INFRASTRUCTURE, TRANSACTIONAL, SCHEDULER, SESSION)
     */
    val category: String,

    /**
     * Specific event type within the category (e.g., USER_REGISTERED, PASSWORD_RESET)
     */
    val type: String,

    /**
     * Event body as JSONB containing event-specific data (userId, email, etc.)
     * Stored as PostgreSQL JSONB for efficient querying and indexing
     * Nullable to support events that don't require body data
     */
    val body: JsonNode? = null,

    /**
     * Additional metadata as JSONB (optional)
     * Used for tracking, debugging, or extra context
     */
    val metadata: JsonNode? = null,

    /**
     * Current status of event processing
     */
    val status: EventStatus = EventStatus.PENDING,

    /**
     * Number of processing attempts
     */
    val attemptCount: Int = 0,

    /**
     * Timestamp of last processing attempt
     */
    val lastAttemptAt: Instant? = null,

    /**
     * Timestamp when event was successfully completed
     */
    val completedAt: Instant? = null,

    /**
     * Timestamp when event was created
     */
    val createdAt: Instant = Instant.now(),

    /**
     * Timestamp when event was last updated
     */
    val updatedAt: Instant = Instant.now()
)

/**
 * Event processing status
 */
enum class EventStatus {
    /**
     * Event is pending processing
     */
    PENDING,

    /**
     * Event is currently being processed
     */
    PROCESSING,

    /**
     * Event was successfully processed
     */
    COMPLETED,

    /**
     * Event processing failed
     */
    FAILED
}
