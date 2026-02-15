package lakho.ecommerce.webservices.event.listeners

import lakho.ecommerce.webservices.auth.services.EmailService
import lakho.ecommerce.webservices.event.events.PasswordResetEvent
import lakho.ecommerce.webservices.event.services.EventService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PasswordResetEventListener(
    private val emailService: EmailService,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(PasswordResetEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePasswordResetEvent(event: PasswordResetEvent) {
        logger.info("Handling PasswordResetEvent: userId={}, email={}", event.userId, event.email)

        // Create event in database and cache
        val savedEvent = eventService.createEvent(
            eventType = "PASSWORD_RESET",
            userId = event.userId,
            email = event.email,
            metadata = mapOf(
                "username" to event.username
            )
        )

        try {
            // Mark as processing
            eventService.markAsProcessing(savedEvent.id)

            // Send password reset confirmation email
            emailService.sendPasswordResetConfirmationEmail(event.email, event.username)

            // Mark as completed and remove from cache
            eventService.markAsCompleted(savedEvent.id)

            logger.info("Password reset confirmation email sent successfully: userId={}, eventId={}", event.userId, savedEvent.id)
        } catch (e: Exception) {
            logger.error("Failed to send password reset confirmation email: userId={}, eventId={}", event.userId, savedEvent.id, e)
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
