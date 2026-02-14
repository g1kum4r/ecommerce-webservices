package lakho.ecommerce.webservices.admin.api.models

import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.entities.Role
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import java.util.UUID

data class UserSummary(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val roles: Set<Role>,
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean
)
