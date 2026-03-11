package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.repositories.OrderItemRepository
import lakho.ecommerce.webservices.consumer.repositories.OrderRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository
) {

    @Transactional(readOnly = true)
    fun getOrders(userId: UUID, page: Int, size: Int, status: String?): CustomPage<OrderListResponse> {
        val offset = page * size

        val (orders, total) = if (!status.isNullOrBlank()) {
            val items = orderRepository.findByUserIdAndStatus(userId, status, size, offset)
            val count = orderRepository.countByUserIdAndStatus(userId, status)
            items to count
        } else {
            val items = orderRepository.findByUserId(userId, size, offset)
            val count = orderRepository.countByUserId(userId)
            items to count
        }

        val responses = orders.map { order ->
            val itemCount = orderItemRepository.findByOrderId(order.id!!).sumOf { it.quantity }
            OrderListResponse.from(order, itemCount)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getOrderById(userId: UUID, orderId: UUID): OrderResponse {
        val order = orderRepository.findByIdAndUserId(orderId, userId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        val items = orderItemRepository.findByOrderId(order.id!!)
        return OrderResponse.from(order, items)
    }

    @Transactional(readOnly = false)
    fun cancelOrder(userId: UUID, orderId: UUID, reason: String?): OrderResponse {
        val order = orderRepository.findByIdAndUserId(orderId, userId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        if (order.status != "PENDING") {
            throw IllegalStateException("Only PENDING orders can be cancelled. Current status: ${order.status}")
        }

        // Restore stock
        val items = orderItemRepository.findByOrderId(order.id!!)
        for (item in items) {
            if (item.variantId != null) {
                val variant = productVariantRepository.findById(item.variantId).orElse(null)
                if (variant != null) {
                    productVariantRepository.save(
                        variant.copy(
                            stockQuantity = variant.stockQuantity + item.quantity,
                            updatedAt = Instant.now()
                        )
                    )
                }
            } else {
                val product = productRepository.findById(item.productId).orElse(null)
                if (product != null) {
                    productRepository.save(
                        product.copy(
                            stockQuantity = product.stockQuantity + item.quantity,
                            updatedAt = Instant.now()
                        )
                    )
                }
            }
        }

        val updated = order.copy(
            status = "CANCELLED",
            cancelledReason = reason,
            cancelledAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val saved = orderRepository.save(updated)
        return OrderResponse.from(saved, items)
    }

    @Transactional(readOnly = false)
    fun confirmDelivery(userId: UUID, orderId: UUID): OrderResponse {
        val order = orderRepository.findByIdAndUserId(orderId, userId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        if (order.status != "DELIVERED") {
            throw IllegalStateException("Only DELIVERED orders can be confirmed. Current status: ${order.status}")
        }

        val updated = order.copy(
            status = "COMPLETED",
            completedAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val saved = orderRepository.save(updated)
        val items = orderItemRepository.findByOrderId(saved.id!!)
        return OrderResponse.from(saved, items)
    }
}
