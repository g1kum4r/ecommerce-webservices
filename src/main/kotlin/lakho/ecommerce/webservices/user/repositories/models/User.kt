package lakho.ecommerce.webservices.user.repositories.models

import lakho.ecommerce.webservices.user.repositories.entities.Role
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val accountExpired: Boolean,
    val accountLocked: Boolean,
    val credentialsExpired: Boolean,
    val enabled: Boolean,
    var roles: Set<Role>,
) {

    constructor(email: String, username: String, passwordHash: String) : this(
        UUID.randomUUID(), email, username, passwordHash, null, false, false, false, false, emptySet()
    )

    constructor(user: lakho.ecommerce.webservices.user.repositories.entities.User) : this(user, emptySet())

    constructor(user: lakho.ecommerce.webservices.user.repositories.entities.User, roles: Set<Role>) : this(
        user.id!!,
        user.email,
        user.username,
        user.firstName,
        user.lastName,
        user.accountExpired,
        user.accountLocked,
        user.credentialsExpired,
        user.enabled,
        roles
    )

    constructor(user: SecureUser, roles: Set<Role>) : this(
        user.id,
        user.email,
        user.username,
        user.firstName,
        user.lastName,
        user.accountExpired,
        user.accountLocked,
        user.credentialsExpired,
        user.enabled,
        roles
    )
}