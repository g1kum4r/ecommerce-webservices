package lakho.ecommerce.webservices.auth.api.models

import lakho.ecommerce.webservices.user.repositories.entities.UserRole

data class RegisterRequest(
    val email: String,
    val password: String,
    val roles: Set<UserRole>
)
