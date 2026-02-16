package lakho.ecommerce.webservices.auth.events

import lakho.ecommerce.webservices.event.events.EmailTemplate
import lakho.ecommerce.webservices.event.events.SendEmailEvent
import java.time.Instant
import java.util.*

/**
 * Event triggered when a user successfully resets their password.
 * Sends a confirmation email to the user.
 */
class PasswordResetEvent(
    source: Any,
    userId: UUID,
    email: String,
    username: String,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : SendEmailEvent(source, userId, email, username, eventId, timestamp) {

    override fun getEventType(): String = "PASSWORD_RESET"

    override fun getEmailTemplate(): EmailTemplate = EmailTemplate.PASSWORD_RESET_CONFIRMATION

    override fun getEmailData(): Map<String, Any> = emptyMap()
}
