package lakho.ecommerce.webservices.auth.api.models

import jakarta.validation.constraints.NotBlank

data class VerifyEmailRequest(
    @field:NotBlank(message = "Token is required")
    val token: String
)
