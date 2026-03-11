package lakho.ecommerce.webservices.storeowner.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class ProductStatus {
    DRAFT, PUBLISHED, ARCHIVED
}

@Table("products")
data class Product(
    @Id val id: UUID? = null,
    val storeId: UUID,
    val categoryId: Long? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val brand: String? = null,
    val basePrice: BigDecimal = BigDecimal.ZERO,
    val salePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val currency: String = "USD",
    val stockQuantity: Int = 0,
    val lowStockThreshold: Int = 5,
    val weight: BigDecimal? = null,
    val weightUnit: String = "kg",
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isDigital: Boolean = false,
    val status: String = "DRAFT",
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val tags: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
