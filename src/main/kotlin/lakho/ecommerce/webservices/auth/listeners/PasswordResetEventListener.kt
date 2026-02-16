package lakho.ecommerce.webservices.auth.listeners

import lakho.ecommerce.webservices.auth.services.EmailService
import lakho.ecommerce.webservices.auth.events.PasswordResetEvent
import lakho.ecommerce.webservices.event.services.EventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Listener for PasswordResetEvent.
 * Sends confirmation email when a user successfully resets their password.
 */
@Component
class PasswordResetEventListener(
    private val emailService: EmailService,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(PasswordResetEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePasswordResetEvent(event: PasswordResetEvent) {
        logger.info(
            "Handling PasswordResetEvent: eventId={}, userId={}, email={}",
            event.eventId,
            event.userId,
            event.email
        )

        // Create event in database and cache using BaseEvent
        val savedEvent = eventService.createEvent(
            baseEvent = event,
            userId = event.userId,
            email = event.email
        )

        try {
            // Mark as processing
            eventService.markAsProcessing(savedEvent.id)

            // Send password reset confirmation email
            emailService.sendPasswordResetConfirmationEmail(event.email, event.username)

            // Mark as completed and remove from cache
            eventService.markAsCompleted(savedEvent.id)

            logger.info(
                "Password reset confirmation email sent successfully: eventId={}, userId={}",
                savedEvent.id,
                event.userId
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to send password reset confirmation email: eventId={}, userId={}",
                savedEvent.id,
                event.userId,
                e
            )
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
