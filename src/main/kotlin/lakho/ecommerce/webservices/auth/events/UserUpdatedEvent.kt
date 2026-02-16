package lakho.ecommerce.webservices.auth.events

import org.springframework.context.ApplicationEvent
import java.util.*

/**
 * Event published when user data is updated.
 * Used to invalidate user data cache in Redis.
 *
 * @property userId User ID
 * @property operation Type of update operation
 */
class UserUpdatedEvent(
    source: Any,
    val userId: UUID,
    val operation: UserOperation
) : ApplicationEvent(source)

enum class UserOperation {
    PROFILE_UPDATED,     // User profile fields updated (firstName, lastName, username, email)
    PASSWORD_CHANGED,    // Password changed (doesn't affect cache)
    ACCOUNT_STATUS_CHANGED, // Account status changed (enabled, locked, expired)
    DELETED              // User deleted
}
