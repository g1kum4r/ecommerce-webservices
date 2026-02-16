package lakho.ecommerce.webservices.common.repositories.models

import lakho.ecommerce.webservices.common.repositories.entities.Role
import java.util.UUID

data class SecureUser(
    val id: UUID,
    val email: String,
    val username: String,
    val passwordHash: String?,
    val firstName: String?,
    val lastName: String?,
    val accountExpired: Boolean,
    val accountLocked: Boolean,
    val credentialsExpired: Boolean,
    val enabled: Boolean,
    var roles: Set<Role>,
) {

    constructor(email: String, username: String, passwordHash: String) : this(
        id = UUID.randomUUID(),
        email = email,
        username = username,
        passwordHash = passwordHash,
        null, null, false, false, false, false, emptySet()
    )

    constructor(user: lakho.ecommerce.webservices.common.repositories.entities.User, roles: Set<Role>) : this(
        user.id!!,
        user.email,
        user.username,
        user.passwordHash,
        user.firstName,
        user.lastName,
        user.accountExpired,
        user.accountLocked,
        user.credentialsExpired,
        user.enabled,
        roles
    )
}

fun SecureUser.toUserModel() = User(this, this.roles)
