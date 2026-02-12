package lakho.ecommerce.webservices.auth.dto

import lakho.ecommerce.webservices.user.UserRole

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: UserRole
)
