package lakho.ecommerce.webservices.consumer.api.models

data class UpdateAddressRequest(
    val label: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val cityId: Long? = null,
    val stateId: Long? = null,
    val countryId: Long? = null,
    val postalCode: String? = null
)
