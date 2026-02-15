# Generic Event System

## Overview

This module provides a generic, extensible event system for the e-commerce application. Events are organized into categories and support asynchronous processing with Redis caching for performance.

## Architecture

### Event Hierarchy

```
BaseEvent (abstract)
│
├── AppFlowEvent (abstract) - Application flow events
│   ├── SendEmailEvent (abstract) - Email-related app flow events
│   │   ├── UserRegisteredEvent - User registration with verification email
│   │   └── PasswordResetEvent - Password reset confirmation email
│   └── [Future: Other app flow events]
│
├── InfrastructureEvent (future) - Service health, deployment events
├── TransactionalEvent (future) - Order, payment events
├── SchedulerEvent (future) - Cron jobs, periodic tasks
└── SessionEvent (future) - Token expiry, logout events
```

### Event Categories

- **APP_FLOW**: Application workflow events (user registration, password reset, etc.)
- **INFRASTRUCTURE**: Service infrastructure events (planned)
- **TRANSACTIONAL**: Business transaction events (planned)
- **SCHEDULER**: Scheduled task events (planned)
- **SESSION**: Session management events (planned)

## Current Implementation

### 1. App Flow Events → Send Email Events

#### UserRegisteredEvent
Triggered when a new user successfully registers.

**Event Type**: `USER_REGISTERED`
**Email Template**: `USER_VERIFICATION`

**Usage**:
```kotlin
applicationEventPublisher.publishEvent(
    UserRegisteredEvent(
        source = this,
        userId = user.id,
        email = user.email,
        username = user.username,
        verificationToken = token
    )
)
```

#### PasswordResetEvent
Triggered when a user successfully resets their password.

**Event Type**: `PASSWORD_RESET`
**Email Template**: `PASSWORD_RESET_CONFIRMATION`

**Usage**:
```kotlin
applicationEventPublisher.publishEvent(
    PasswordResetEvent(
        source = this,
        userId = user.id,
        email = user.email,
        username = user.username
    )
)
```

## Adding New Event Types

### Example: Adding a Welcome Email Event

#### Step 1: Create the Event Class

```kotlin
// src/main/kotlin/lakho/ecommerce/webservices/event/events/WelcomeEmailEvent.kt
class WelcomeEmailEvent(
    source: Any,
    userId: UUID,
    email: String,
    username: String,
    val planName: String,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : SendEmailEvent(source, userId, email, username, eventId, timestamp) {

    override fun getEventType(): String = "WELCOME_EMAIL"

    override fun getEmailTemplate(): EmailTemplate = EmailTemplate.WELCOME_EMAIL

    override fun getEmailData(): Map<String, Any> = mapOf(
        "planName" to planName
    )
}
```

#### Step 2: Create the Event Listener

```kotlin
// src/main/kotlin/lakho/ecommerce/webservices/event/listeners/WelcomeEmailEventListener.kt
@Component
class WelcomeEmailEventListener(
    private val emailService: EmailService,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(WelcomeEmailEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleWelcomeEmailEvent(event: WelcomeEmailEvent) {
        logger.info("Handling WelcomeEmailEvent: eventId={}, userId={}", event.eventId, event.userId)

        val savedEvent = eventService.createEvent(
            baseEvent = event,
            userId = event.userId,
            email = event.email
        )

        try {
            eventService.markAsProcessing(savedEvent.id)
            emailService.sendWelcomeEmail(event.email, event.username, event.planName)
            eventService.markAsCompleted(savedEvent.id)
            logger.info("Welcome email sent successfully: eventId={}", savedEvent.id)
        } catch (e: Exception) {
            logger.error("Failed to send welcome email: eventId={}", savedEvent.id, e)
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
```

#### Step 3: Publish the Event

```kotlin
applicationEventPublisher.publishEvent(
    WelcomeEmailEvent(
        source = this,
        userId = user.id,
        email = user.email,
        username = user.username,
        planName = "Premium"
    )
)
```

### Example: Adding a Different Event Category (Session Events)

#### Step 1: Create Base Event Class for Session Category

```kotlin
// src/main/kotlin/lakho/ecommerce/webservices/event/events/SessionEvent.kt
abstract class SessionEvent(
    source: Any,
    open val sessionId: String,
    open val userId: UUID,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : BaseEvent(source, eventId, timestamp) {

    override fun getEventCategory(): EventCategory = EventCategory.SESSION
}
```

#### Step 2: Create Specific Session Event

```kotlin
// src/main/kotlin/lakho/ecommerce/webservices/event/events/SessionExpiredEvent.kt
class SessionExpiredEvent(
    source: Any,
    sessionId: String,
    userId: UUID,
    val reason: String,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : SessionEvent(source, sessionId, userId, eventId, timestamp) {

    override fun getEventType(): String = "SESSION_EXPIRED"

    override fun getMetadata(): Map<String, Any> = mapOf(
        "sessionId" to sessionId,
        "reason" to reason
    )
}
```

#### Step 3: Create Event Listener

```kotlin
@Component
class SessionExpiredEventListener(
    private val eventService: EventService,
    private val sessionCleanupService: SessionCleanupService
) {
    private val logger = LoggerFactory.getLogger(SessionExpiredEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleSessionExpiredEvent(event: SessionExpiredEvent) {
        logger.info("Handling SessionExpiredEvent: eventId={}, sessionId={}", event.eventId, event.sessionId)

        val savedEvent = eventService.createEvent(
            baseEvent = event,
            userId = event.userId,
            email = null // Session events don't require email
        )

        try {
            eventService.markAsProcessing(savedEvent.id)
            sessionCleanupService.cleanupSession(event.sessionId)
            eventService.markAsCompleted(savedEvent.id)
            logger.info("Session cleaned up successfully: eventId={}", savedEvent.id)
        } catch (e: Exception) {
            logger.error("Failed to cleanup session: eventId={}", savedEvent.id, e)
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
```

## Event Processing Flow

1. **Event Publication**: Service publishes event using `ApplicationEventPublisher`
2. **Event Listener**: `@TransactionalEventListener` listens after transaction commit
3. **Event Persistence**: `EventService.createEvent()` saves event to database and Redis
4. **Processing**: Event listener marks event as PROCESSING and executes business logic
5. **Completion**: Event is marked as COMPLETED (or FAILED) and removed from cache

## Event Statuses

- **PENDING**: Event created but not yet processed
- **PROCESSING**: Event is currently being processed
- **COMPLETED**: Event successfully processed
- **FAILED**: Event processing failed

## Caching Strategy

Events are cached in Redis with the following keys:
- `event:{eventId}` - Individual event cache
- `event:category:{category}` - Set of event IDs by category
- `event:type:{eventType}:{userId}` - Latest event by type and user

**Cache TTL**: 24 hours

## Database Schema

```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    event_category VARCHAR(50) NOT NULL,      -- APP_FLOW, INFRASTRUCTURE, etc.
    event_type VARCHAR(100) NOT NULL,         -- USER_REGISTERED, PASSWORD_RESET, etc.
    user_id UUID,                             -- Optional, depends on event type
    email VARCHAR(255),                       -- Optional, for email-related events
    metadata TEXT,                            -- JSON metadata specific to event type
    status VARCHAR(20) NOT NULL,              -- PENDING, PROCESSING, COMPLETED, FAILED
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Best Practices

1. **Event Naming**: Use past tense for event names (e.g., `UserRegisteredEvent`, not `UserRegisterEvent`)
2. **Async Processing**: Always use `@Async` on event listeners for non-blocking execution
3. **Transaction Safety**: Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
4. **Error Handling**: Always wrap event processing in try-catch and mark as FAILED on errors
5. **Logging**: Log event IDs for traceability
6. **Idempotency**: Design event handlers to be idempotent (safe to retry)

## Future Enhancements

- **Retry Mechanism**: Automatic retry for failed events with exponential backoff
- **Dead Letter Queue**: Move permanently failed events to DLQ
- **Event Scheduling**: Support for delayed event processing
- **Event Filtering**: Advanced filtering by category, type, status
- **Event Metrics**: Monitor event processing times and failure rates
- **Event Replay**: Ability to replay historical events
