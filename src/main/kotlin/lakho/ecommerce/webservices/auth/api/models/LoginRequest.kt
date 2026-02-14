package lakho.ecommerce.webservices.auth.api.models

data class LoginRequest(
    val email: String,
    val password: String
)
