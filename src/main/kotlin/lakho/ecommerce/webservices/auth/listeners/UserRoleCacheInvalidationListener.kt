package lakho.ecommerce.webservices.auth.listeners

import lakho.ecommerce.webservices.auth.events.UserRoleUpdatedEvent
import lakho.ecommerce.webservices.common.services.UserRoleCacheService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Listener for UserRoleUpdatedEvent.
 * Invalidates user roles cache when roles are updated.
 *
 * Uses @TransactionalEventListener to ensure cache invalidation only happens
 * AFTER the transaction commits successfully, preventing cache inconsistency.
 */
@Component
class UserRoleCacheInvalidationListener(
    private val userRoleCacheService: UserRoleCacheService
) {
    private val logger = LoggerFactory.getLogger(UserRoleCacheInvalidationListener::class.java)

    /**
     * Handle role update events after transaction commits.
     *
     * @Async: Runs asynchronously to not block the main transaction thread
     * @TransactionalEventListener(phase = AFTER_COMMIT): Only invalidates cache after DB commit
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRoleUpdated(event: UserRoleUpdatedEvent) {
        logger.info(
            "Handling UserRoleUpdatedEvent: userId={}, operation={}",
            event.userId,
            event.operation
        )

        userRoleCacheService.invalidateUserRolesCache(event.userId)

        logger.debug("Cache invalidated for user: userId={}", event.userId)
    }
}
