package lakho.ecommerce.webservices.event.repositories

import lakho.ecommerce.webservices.event.repositories.entities.Event
import lakho.ecommerce.webservices.event.repositories.entities.EventStatus
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface EventRepository : CrudRepository<Event, UUID> {

    @Query("SELECT * FROM events WHERE type = :eventType AND body->>'userId' = :userId ORDER BY created_at DESC LIMIT 1")
    fun findLatestByEventTypeAndUserId(
        @Param("eventType") eventType: String,
        @Param("userId") userId: String
    ): Event?

    @Query("SELECT * FROM events WHERE status = :status ORDER BY created_at ASC")
    fun findByStatus(@Param("status") status: String): List<Event>

    @Query("SELECT * FROM events WHERE category = :category AND status = :status ORDER BY created_at ASC")
    fun findByEventCategoryAndStatus(
        @Param("category") category: String,
        @Param("status") status: String
    ): List<Event>

    @Modifying
    @Query("""
        UPDATE events
        SET status = :status,
            attempt_count = :attemptCount,
            last_attempt_at = :lastAttemptAt,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    fun updateAttempt(
        @Param("id") id: UUID,
        @Param("status") status: String,
        @Param("attemptCount") attemptCount: Int,
        @Param("lastAttemptAt") lastAttemptAt: Instant,
        @Param("updatedAt") updatedAt: Instant
    )

    @Modifying
    @Query("""
        UPDATE events
        SET status = :status,
            completed_at = :completedAt,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    fun markAsCompleted(
        @Param("id") id: UUID,
        @Param("status") status: String,
        @Param("completedAt") completedAt: Instant,
        @Param("updatedAt") updatedAt: Instant
    )
}
