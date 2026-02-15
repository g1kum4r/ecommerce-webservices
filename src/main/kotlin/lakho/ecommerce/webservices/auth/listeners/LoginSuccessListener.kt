package lakho.ecommerce.webservices.auth.listeners

import lakho.ecommerce.webservices.auth.events.LoginSuccessEvent
import lakho.ecommerce.webservices.auth.services.JwtTokenCacheService
import lakho.ecommerce.webservices.user.services.CachedUserData
import lakho.ecommerce.webservices.user.services.UserDataCacheService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Listener for LoginSuccessEvent.
 * Caches JWT tokens and user data in Redis for fast authentication validation.
 */
@Component
class LoginSuccessListener(
    private val jwtTokenCacheService: JwtTokenCacheService,
    private val userDataCacheService: UserDataCacheService
) {
    private val logger = LoggerFactory.getLogger(LoginSuccessListener::class.java)

    @Async
    @EventListener
    fun handleLoginSuccess(event: LoginSuccessEvent) {
        logger.info("Handling LoginSuccessEvent: userId={}, email={}", event.user.id, event.user.email)

        try {
            // Cache JWT tokens
            jwtTokenCacheService.cacheTokens(
                accessToken = event.accessToken,
                refreshToken = event.refreshToken,
                email = event.user.email
            )
            logger.info("Successfully cached tokens for user: userId={}, email={}", event.user.id, event.user.email)

            // Cache user data (all fields except password)
            val cachedUserData = CachedUserData(
                id = event.user.id,
                email = event.user.email,
                username = event.user.username,
                firstName = event.user.firstName,
                lastName = event.user.lastName,
                accountExpired = event.user.accountExpired,
                accountLocked = event.user.accountLocked,
                credentialsExpired = event.user.credentialsExpired,
                enabled = event.user.enabled,
                roles = event.user.roles
            )
            userDataCacheService.cacheUserData(cachedUserData)
            logger.info("Successfully cached user data: userId={}, email={}", event.user.id, event.user.email)
        } catch (e: Exception) {
            logger.error("Failed to cache tokens/user data: userId={}, email={}", event.user.id, event.user.email, e)
            // Don't fail login if caching fails - it's a performance optimization
        }
    }
}
