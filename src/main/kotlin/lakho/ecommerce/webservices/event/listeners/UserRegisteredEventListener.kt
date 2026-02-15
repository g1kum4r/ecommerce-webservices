package lakho.ecommerce.webservices.event.listeners

import lakho.ecommerce.webservices.auth.services.EmailService
import lakho.ecommerce.webservices.event.events.UserRegisteredEvent
import lakho.ecommerce.webservices.event.services.EventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Listener for UserRegisteredEvent.
 * Sends verification email when a new user registers.
 */
@Component
class UserRegisteredEventListener(
    private val emailService: EmailService,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(UserRegisteredEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegisteredEvent(event: UserRegisteredEvent) {
        logger.info(
            "Handling UserRegisteredEvent: eventId={}, userId={}, email={}",
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

            // Send verification email
            emailService.sendVerificationEmail(event.email, event.username, event.verificationToken)

            // Mark as completed and remove from cache
            eventService.markAsCompleted(savedEvent.id)

            logger.info(
                "Verification email sent successfully: eventId={}, userId={}",
                savedEvent.id,
                event.userId
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to send verification email: eventId={}, userId={}",
                savedEvent.id,
                event.userId,
                e
            )
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
