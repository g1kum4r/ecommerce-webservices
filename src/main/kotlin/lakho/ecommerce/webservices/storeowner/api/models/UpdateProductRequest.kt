package lakho.ecommerce.webservices.storeowner.api.models

import java.math.BigDecimal

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val categoryId: Long? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val brand: String? = null,
    val basePrice: BigDecimal? = null,
    val salePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val currency: String? = null,
    val stockQuantity: Int? = null,
    val lowStockThreshold: Int? = null,
    val weight: BigDecimal? = null,
    val weightUnit: String? = null,
    val isDigital: Boolean? = null,
    val isFeatured: Boolean? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val tags: String? = null
)
