package lakho.ecommerce.webservices.event.listeners

import lakho.ecommerce.webservices.auth.services.EmailService
import lakho.ecommerce.webservices.event.events.UserRegisteredEvent
import lakho.ecommerce.webservices.event.services.EventService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserRegisteredEventListener(
    private val emailService: EmailService,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(UserRegisteredEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegisteredEvent(event: UserRegisteredEvent) {
        logger.info("Handling UserRegisteredEvent: userId={}, email={}", event.userId, event.email)

        // Create event in database and cache
        val savedEvent = eventService.createEvent(
            eventType = "USER_REGISTERED",
            userId = event.userId,
            email = event.email,
            metadata = mapOf(
                "username" to event.username,
                "verificationToken" to event.verificationToken
            )
        )

        try {
            // Mark as processing
            eventService.markAsProcessing(savedEvent.id)

            // Send verification email
            emailService.sendVerificationEmail(event.email, event.username, event.verificationToken)

            // Mark as completed and remove from cache
            eventService.markAsCompleted(savedEvent.id)

            logger.info("Verification email sent successfully: userId={}, eventId={}", event.userId, savedEvent.id)
        } catch (e: Exception) {
            logger.error("Failed to send verification email: userId={}, eventId={}", event.userId, savedEvent.id, e)
            eventService.markAsFailed(savedEvent.id)
        }
    }
}
