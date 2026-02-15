package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.repositories.entities.Role
import java.io.Serializable
import java.util.*

/**
 * Cached user data DTO for Redis storage.
 * Contains all user information except password hash.
 *
 * Used for:
 * - Fast user profile retrieval
 * - Authorization checks (roles)
 * - User display information
 * - Account status checks
 *
 * This is cached in Redis with 24-hour TTL.
 * Password hash is never cached for security reasons.
 */
data class CachedUserData(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val accountExpired: Boolean,
    val accountLocked: Boolean,
    val credentialsExpired: Boolean,
    val enabled: Boolean,
    val roles: Set<Role>
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
