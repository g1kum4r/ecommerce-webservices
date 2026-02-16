package lakho.ecommerce.webservices.common.event.events

import java.time.Instant
import java.util.*

/**
 * Base class for application flow events.
 * These events represent user-facing application workflows.
 */
abstract class AppFlowEvent(
    source: Any,
    open val userId: UUID,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : BaseEvent(source, eventId, timestamp) {

    override fun getEventCategory(): EventCategory = EventCategory.APP_FLOW
}

/**
 * Base class for email-related application flow events.
 * These events trigger email sending operations.
 */
abstract class SendEmailEvent(
    source: Any,
    userId: UUID,
    open val email: String,
    open val username: String,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : AppFlowEvent(source, userId, eventId, timestamp) {

    /**
     * Returns the email template to use
     */
    abstract fun getEmailTemplate(): EmailTemplate

    /**
     * Returns email-specific data for template rendering
     */
    abstract fun getEmailData(): Map<String, Any>

    override fun getMetadata(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "emailTemplate" to getEmailTemplate().name
        ) + getEmailData()
    }
}

/**
 * Email templates used in the application
 */
enum class EmailTemplate {
    USER_VERIFICATION,
    PASSWORD_RESET_CONFIRMATION,
    PASSWORD_RESET_REQUEST,
    WELCOME_EMAIL,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED
}
