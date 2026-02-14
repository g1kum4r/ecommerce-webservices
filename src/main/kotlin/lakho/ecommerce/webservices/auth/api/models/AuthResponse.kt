package lakho.ecommerce.webservices.auth.api.models

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
