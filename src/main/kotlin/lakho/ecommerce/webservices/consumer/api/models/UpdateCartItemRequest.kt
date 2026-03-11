package lakho.ecommerce.webservices.consumer.api.models

import jakarta.validation.constraints.Min

data class UpdateCartItemRequest(
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)
