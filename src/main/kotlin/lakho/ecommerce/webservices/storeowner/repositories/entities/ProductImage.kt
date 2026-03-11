package lakho.ecommerce.webservices.storeowner.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("product_images")
data class ProductImage(
    @Id val id: UUID? = null,
    val productId: UUID,
    val imageUrl: String,
    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val createdAt: Instant = Instant.now()
)
