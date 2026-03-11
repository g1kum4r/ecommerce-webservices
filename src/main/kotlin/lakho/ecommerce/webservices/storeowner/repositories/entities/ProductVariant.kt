package lakho.ecommerce.webservices.storeowner.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Table("product_variants")
data class ProductVariant(
    @Id val id: UUID? = null,
    val productId: UUID,
    val name: String,
    val sku: String? = null,
    val price: BigDecimal,
    val stockQuantity: Int = 0,
    val attributes: String = "{}",
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
