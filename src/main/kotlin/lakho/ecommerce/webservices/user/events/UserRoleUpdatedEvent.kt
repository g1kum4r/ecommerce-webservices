package lakho.ecommerce.webservices.user.events

import org.springframework.context.ApplicationEvent
import java.util.*

/**
 * Event published when user roles are updated.
 * Triggers cache invalidation for affected users.
 */
class UserRoleUpdatedEvent(
    source: Any,
    val userId: UUID,
    val operation: RoleOperation
) : ApplicationEvent(source)

enum class RoleOperation {
    ASSIGNED,   // Roles assigned to user
    REMOVED,    // Roles removed from user
    MODIFIED    // User's roles modified
}
