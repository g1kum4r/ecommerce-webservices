package lakho.ecommerce.webservices.consumer.api.models

import java.time.LocalDate
import java.util.UUID

data class ConsumerProfileResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val avatarUrl: String?
)
