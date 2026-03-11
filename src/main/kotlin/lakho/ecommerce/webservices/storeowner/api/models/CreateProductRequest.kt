package lakho.ecommerce.webservices.storeowner.api.models

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    val name: String,

    val description: String? = null,
    val shortDescription: String? = null,
    val categoryId: Long? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val brand: String? = null,

    @field:NotNull(message = "Base price is required")
    @field:DecimalMin(value = "0.0", message = "Base price must be non-negative")
    val basePrice: BigDecimal,

    val salePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val currency: String = "USD",

    @field:Min(value = 0, message = "Stock quantity must be non-negative")
    val stockQuantity: Int = 0,

    val lowStockThreshold: Int = 5,
    val weight: BigDecimal? = null,
    val weightUnit: String = "kg",
    val isDigital: Boolean = false,
    val isFeatured: Boolean = false,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val tags: String? = null
)
