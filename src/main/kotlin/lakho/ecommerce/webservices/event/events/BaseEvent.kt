package lakho.ecommerce.webservices.event.events

import org.springframework.context.ApplicationEvent
import java.time.Instant
import java.util.*

/**
 * Base class for all application events.
 * Provides common properties and functionality for all event types.
 */
abstract class BaseEvent(
    source: Any,
    open val eventId: UUID = UUID.randomUUID(),
    open val timestamp: Instant = Instant.now()
) : ApplicationEvent(source) {

    /**
     * Returns the event category (e.g., APP_FLOW, INFRASTRUCTURE, TRANSACTIONAL, SCHEDULER, SESSION)
     */
    abstract fun getEventCategory(): EventCategory

    /**
     * Returns the specific event type within the category
     */
    abstract fun getEventType(): String

    /**
     * Returns metadata specific to this event for persistence
     */
    abstract fun getMetadata(): Map<String, Any>
}

/**
 * Event categories for organizing different types of events
 */
enum class EventCategory {
    /**
     * Application flow events (user registration, password reset, etc.)
     */
    APP_FLOW,

    /**
     * Infrastructure events (service health, deployment, etc.)
     */
    INFRASTRUCTURE,

    /**
     * Transactional events (order created, payment processed, etc.)
     */
    TRANSACTIONAL,

    /**
     * Scheduled events (cron jobs, periodic tasks, etc.)
     */
    SCHEDULER,

    /**
     * Session management events (token expiry, logout, etc.)
     */
    SESSION
}
