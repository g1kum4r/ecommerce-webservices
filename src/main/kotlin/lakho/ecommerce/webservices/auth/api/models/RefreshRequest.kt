package lakho.ecommerce.webservices.auth.api.models

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)
