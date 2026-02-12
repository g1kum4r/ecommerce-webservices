package lakho.ecommerce.webservices.admin.dto

import lakho.ecommerce.webservices.user.UserRole

data class UserSummary(
    val id: Long,
    val email: String,
    val role: UserRole,
    val active: Boolean
)
