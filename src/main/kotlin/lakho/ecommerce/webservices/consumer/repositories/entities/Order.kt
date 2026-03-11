package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Table("orders")
data class Order(
    @Id val id: UUID? = null,
    val orderNumber: String,
    val userId: UUID,
    val storeId: UUID,
    val shippingAddressSnapshot: String,
    val subtotal: BigDecimal,
    val shippingFee: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val currency: String = "USD",
    val status: String = "PENDING",
    val paymentStatus: String = "UNPAID",
    val paymentMethod: String? = null,
    val notes: String? = null,
    val cancelledReason: String? = null,
    val confirmedAt: Instant? = null,
    val shippedAt: Instant? = null,
    val deliveredAt: Instant? = null,
    val completedAt: Instant? = null,
    val cancelledAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
