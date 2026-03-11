package lakho.ecommerce.webservices.consumer.api.models

import java.time.LocalDate

data class UpdateConsumerProfileRequest(
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val avatarUrl: String? = null
)
