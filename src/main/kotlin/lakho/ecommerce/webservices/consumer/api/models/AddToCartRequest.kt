package lakho.ecommerce.webservices.consumer.api.models

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,
    val variantId: UUID? = null,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1
)
