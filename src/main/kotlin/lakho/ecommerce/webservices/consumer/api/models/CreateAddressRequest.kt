package lakho.ecommerce.webservices.consumer.api.models

import jakarta.validation.constraints.NotBlank

data class CreateAddressRequest(
    val label: String = "Home",
    @field:NotBlank(message = "Recipient name is required")
    val recipientName: String,
    @field:NotBlank(message = "Phone is required")
    val phone: String,
    @field:NotBlank(message = "Address line 1 is required")
    val addressLine1: String,
    val addressLine2: String? = null,
    val cityId: Long? = null,
    val stateId: Long? = null,
    val countryId: Long? = null,
    val postalCode: String? = null,
    val isDefault: Boolean = false
)
