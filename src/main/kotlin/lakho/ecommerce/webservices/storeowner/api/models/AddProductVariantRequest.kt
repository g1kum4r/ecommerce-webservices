package lakho.ecommerce.webservices.storeowner.api.models

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class AddProductVariantRequest(
    @field:NotBlank(message = "Variant name is required")
    val name: String,

    val sku: String? = null,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", message = "Price must be non-negative")
    val price: BigDecimal,

    val stockQuantity: Int = 0,
    val attributes: String = "{}"
)

data class UpdateProductVariantRequest(
    val name: String? = null,
    val sku: String? = null,
    val price: BigDecimal? = null,
    val stockQuantity: Int? = null,
    val attributes: String? = null,
    val isActive: Boolean? = null
)
