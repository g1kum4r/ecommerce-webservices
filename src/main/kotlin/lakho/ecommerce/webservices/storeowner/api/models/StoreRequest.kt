package lakho.ecommerce.webservices.storeowner.api.models

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import lakho.ecommerce.webservices.storeowner.repositories.entities.LocationType
import java.math.BigDecimal
import java.util.*

data class CreateStoreRequest(
    @field:NotBlank(message = "Store name is required")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "Store category is required")
    val storeCategoryId: Long,

    @field:NotNull(message = "Location type is required")
    val locationType: LocationType,

    // Address reference (optional, for physical locations)
    val addressId: UUID? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,

    // Contact
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val businessHours: String? = null
)

data class UpdateStoreRequest(
    val name: String? = null,
    val description: String? = null,
    val storeCategoryId: Long? = null,
    val locationType: LocationType? = null,
    val addressId: UUID? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val businessHours: String? = null
)
