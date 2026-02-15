package lakho.ecommerce.webservices.auth.listeners

import lakho.ecommerce.webservices.auth.events.LoginSuccessEvent
import lakho.ecommerce.webservices.auth.services.JwtTokenCacheService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Listener for LoginSuccessEvent.
 * Caches JWT tokens in Redis for fast authentication validation.
 */
@Component
class LoginSuccessListener(
    private val jwtTokenCacheService: JwtTokenCacheService
) {
    private val logger = LoggerFactory.getLogger(LoginSuccessListener::class.java)

    @Async
    @EventListener
    fun handleLoginSuccess(event: LoginSuccessEvent) {
        logger.info("Handling LoginSuccessEvent: email={}", event.email)

        try {
            jwtTokenCacheService.cacheTokens(
                accessToken = event.accessToken,
                refreshToken = event.refreshToken,
                email = event.email
            )
            logger.info("Successfully cached tokens for user: email={}", event.email)
        } catch (e: Exception) {
            logger.error("Failed to cache tokens for user: email={}", event.email, e)
            // Don't fail login if caching fails - it's a performance optimization
        }
    }
}
