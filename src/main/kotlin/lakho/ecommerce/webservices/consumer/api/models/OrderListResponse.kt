package lakho.ecommerce.webservices.consumer.api.models

import lakho.ecommerce.webservices.consumer.repositories.entities.Order
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderListResponse(
    val id: UUID,
    val orderNumber: String,
    val storeId: UUID,
    val subtotal: BigDecimal,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String?,
    val itemCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(order: Order, itemCount: Int): OrderListResponse {
            return OrderListResponse(
                id = order.id!!,
                orderNumber = order.orderNumber,
                storeId = order.storeId,
                subtotal = order.subtotal,
                totalAmount = order.totalAmount,
                currency = order.currency,
                status = order.status,
                paymentStatus = order.paymentStatus,
                paymentMethod = order.paymentMethod,
                itemCount = itemCount,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}
