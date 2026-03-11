package lakho.ecommerce.webservices.consumer.api.models

import jakarta.validation.constraints.NotBlank

data class UpdateOrderStatusRequest(
    @field:NotBlank val status: String
)
