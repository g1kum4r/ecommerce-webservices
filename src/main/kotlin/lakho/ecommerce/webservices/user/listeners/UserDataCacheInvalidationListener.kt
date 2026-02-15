package lakho.ecommerce.webservices.user.listeners

import lakho.ecommerce.webservices.user.events.UserOperation
import lakho.ecommerce.webservices.user.events.UserUpdatedEvent
import lakho.ecommerce.webservices.user.services.UserDataCacheService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Listener for UserUpdatedEvent.
 * Invalidates user data cache in Redis after successful transaction commit.
 *
 * Uses @TransactionalEventListener to ensure cache invalidation only happens
 * after the database transaction is committed successfully.
 */
@Component
class UserDataCacheInvalidationListener(
    private val userDataCacheService: UserDataCacheService
) {
    private val logger = LoggerFactory.getLogger(UserDataCacheInvalidationListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserUpdated(event: UserUpdatedEvent) {
        logger.info("Handling UserUpdatedEvent: userId={}, operation={}", event.userId, event.operation)

        try {
            when (event.operation) {
                UserOperation.PROFILE_UPDATED,
                UserOperation.ACCOUNT_STATUS_CHANGED,
                UserOperation.DELETED -> {
                    // Invalidate cache to force reload with fresh data
                    userDataCacheService.invalidateUserDataCache(event.userId)
                    logger.info("Invalidated user data cache: userId={}, operation={}", event.userId, event.operation)
                }
                UserOperation.PASSWORD_CHANGED -> {
                    // Password change doesn't affect cached data, no need to invalidate
                    logger.debug("Password changed, cache not invalidated: userId={}", event.userId)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate user data cache: userId={}, operation={}", event.userId, event.operation, e)
            // Don't fail the transaction if cache invalidation fails
        }
    }
}
