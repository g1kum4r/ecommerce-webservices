package lakho.ecommerce.webservices.auth.dto

data class LoginRequest(
    val email: String,
    val password: String
)
