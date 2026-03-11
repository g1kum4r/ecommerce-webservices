package lakho.ecommerce.webservices.consumer.api.models

import lakho.ecommerce.webservices.consumer.repositories.entities.Order
import lakho.ecommerce.webservices.consumer.repositories.entities.OrderItem
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val orderNumber: String,
    val userId: UUID,
    val storeId: UUID,
    val shippingAddressSnapshot: String,
    val subtotal: BigDecimal,
    val shippingFee: BigDecimal,
    val taxAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String?,
    val notes: String?,
    val cancelledReason: String?,
    val items: List<OrderItemResponse>,
    val confirmedAt: Instant?,
    val shippedAt: Instant?,
    val deliveredAt: Instant?,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(order: Order, items: List<OrderItem>): OrderResponse {
            return OrderResponse(
                id = order.id!!,
                orderNumber = order.orderNumber,
                userId = order.userId,
                storeId = order.storeId,
                shippingAddressSnapshot = order.shippingAddressSnapshot,
                subtotal = order.subtotal,
                shippingFee = order.shippingFee,
                taxAmount = order.taxAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                currency = order.currency,
                status = order.status,
                paymentStatus = order.paymentStatus,
                paymentMethod = order.paymentMethod,
                notes = order.notes,
                cancelledReason = order.cancelledReason,
                items = items.map { OrderItemResponse.from(it) },
                confirmedAt = order.confirmedAt,
                shippedAt = order.shippedAt,
                deliveredAt = order.deliveredAt,
                completedAt = order.completedAt,
                cancelledAt = order.cancelledAt,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}
