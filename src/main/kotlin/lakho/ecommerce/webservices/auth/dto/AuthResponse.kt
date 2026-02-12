package lakho.ecommerce.webservices.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
