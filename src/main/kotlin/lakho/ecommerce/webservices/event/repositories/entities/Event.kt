package lakho.ecommerce.webservices.event.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("events")
data class Event(
    @Id val id: UUID = UUID.randomUUID(),
    val eventType: String,
    val userId: UUID,
    val email: String,
    val metadata: String? = null,
    val status: EventStatus = EventStatus.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptAt: Instant? = null,
    val completedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class EventStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
