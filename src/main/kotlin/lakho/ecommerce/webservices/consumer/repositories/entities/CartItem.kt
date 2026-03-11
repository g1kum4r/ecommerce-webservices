package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Table("cart_items")
data class CartItem(
    @Id val id: UUID? = null,
    val cartId: UUID,
    val productId: UUID,
    val variantId: UUID? = null,
    val quantity: Int = 1,
    val unitPrice: BigDecimal,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
