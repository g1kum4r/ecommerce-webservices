package lakho.ecommerce.webservices.consumer.api.models

import java.util.UUID

data class ConsumerProfile(
    val id: UUID,
    val email: String
)
