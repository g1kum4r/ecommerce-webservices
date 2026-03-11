package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Table("order_items")
data class OrderItem(
    @Id val id: UUID? = null,
    val orderId: UUID,
    val productId: UUID,
    val variantId: UUID? = null,
    val productName: String,
    val variantName: String? = null,
    val productImageUrl: String? = null,
    val sku: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: Instant = Instant.now()
)
