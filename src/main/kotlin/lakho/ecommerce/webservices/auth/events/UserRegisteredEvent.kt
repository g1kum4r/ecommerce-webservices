package lakho.ecommerce.webservices.auth.events

import lakho.ecommerce.webservices.common.event.events.EmailTemplate
import lakho.ecommerce.webservices.common.event.events.SendEmailEvent
import java.time.Instant
import java.util.*

/**
 * Event triggered when a new user successfully registers.
 * Sends a verification email to the user.
 */
class UserRegisteredEvent(
    source: Any,
    userId: UUID,
    email: String,
    username: String,
    val verificationToken: String,
    eventId: UUID = UUID.randomUUID(),
    timestamp: Instant = Instant.now()
) : SendEmailEvent(source, userId, email, username, eventId, timestamp) {

    override fun getEventType(): String = "USER_REGISTERED"

    override fun getEmailTemplate(): EmailTemplate = EmailTemplate.USER_VERIFICATION

    override fun getEmailData(): Map<String, Any> = mapOf(
        "verificationToken" to verificationToken
    )
}
