package lakho.ecommerce.webservices.consumer.api.models

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CheckoutRequest(
    @field:NotNull val addressId: UUID,
    val paymentMethod: String = "COD",
    val notes: String? = null
)
