package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderStatsResponse
import lakho.ecommerce.webservices.consumer.repositories.OrderItemRepository
import lakho.ecommerce.webservices.consumer.repositories.OrderRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreOwnerProfileRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OrderManagementService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val storeRepository: StoreRepository,
    private val storeOwnerProfileRepository: StoreOwnerProfileRepository
) {

    private val validTransitions = mapOf(
        "PENDING" to "CONFIRMED",
        "CONFIRMED" to "PROCESSING",
        "PROCESSING" to "SHIPPED",
        "SHIPPED" to "DELIVERED"
    )

    @Transactional(readOnly = true)
    fun getStoreOrders(userId: UUID, page: Int, size: Int, status: String?): CustomPage<OrderListResponse> {
        val storeId = getStoreIdForUser(userId)
        val offset = page * size

        val (orders, total) = if (!status.isNullOrBlank()) {
            val items = orderRepository.findByStoreIdAndStatus(storeId, status, size, offset)
            val count = orderRepository.countByStoreIdAndStatus(storeId, status)
            items to count
        } else {
            val items = orderRepository.findByStoreId(storeId, size, offset)
            val count = orderRepository.countByStoreId(storeId)
            items to count
        }

        val responses = orders.map { order ->
            val itemCount = orderItemRepository.findByOrderId(order.id!!).sumOf { it.quantity }
            OrderListResponse.from(order, itemCount)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getStoreOrderById(userId: UUID, orderId: UUID): OrderResponse {
        val storeId = getStoreIdForUser(userId)
        val order = orderRepository.findByIdAndStoreId(orderId, storeId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        val items = orderItemRepository.findByOrderId(order.id!!)
        return OrderResponse.from(order, items)
    }

    @Transactional(readOnly = false)
    fun updateOrderStatus(userId: UUID, orderId: UUID, newStatus: String): OrderResponse {
        val storeId = getStoreIdForUser(userId)
        val order = orderRepository.findByIdAndStoreId(orderId, storeId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        val expectedNext = validTransitions[order.status]
            ?: throw IllegalStateException("Cannot transition from status: ${order.status}")

        if (newStatus != expectedNext) {
            throw IllegalStateException("Invalid status transition: ${order.status} -> $newStatus. Expected: $expectedNext")
        }

        val now = Instant.now()
        val updated = when (newStatus) {
            "CONFIRMED" -> order.copy(status = newStatus, confirmedAt = now, updatedAt = now)
            "PROCESSING" -> order.copy(status = newStatus, updatedAt = now)
            "SHIPPED" -> order.copy(status = newStatus, shippedAt = now, updatedAt = now)
            "DELIVERED" -> order.copy(status = newStatus, deliveredAt = now, updatedAt = now)
            else -> throw IllegalStateException("Unexpected status: $newStatus")
        }

        val saved = orderRepository.save(updated)
        val items = orderItemRepository.findByOrderId(saved.id!!)
        return OrderResponse.from(saved, items)
    }

    @Transactional(readOnly = true)
    fun getOrderStats(userId: UUID): OrderStatsResponse {
        val storeId = getStoreIdForUser(userId)

        val pending = orderRepository.countPendingByStoreId(storeId)
        val confirmed = orderRepository.countConfirmedByStoreId(storeId)
        val processing = orderRepository.countProcessingByStoreId(storeId)
        val shipped = orderRepository.countShippedByStoreId(storeId)
        val delivered = orderRepository.countDeliveredByStoreId(storeId)
        val completed = orderRepository.countCompletedByStoreId(storeId)
        val cancelled = orderRepository.countCancelledByStoreId(storeId)
        val total = pending + confirmed + processing + shipped + delivered + completed + cancelled

        return OrderStatsResponse(
            total = total,
            pending = pending,
            confirmed = confirmed,
            processing = processing,
            shipped = shipped,
            delivered = delivered,
            completed = completed,
            cancelled = cancelled
        )
    }

    private fun getStoreIdForUser(userId: UUID): UUID {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val stores = storeRepository.findAllByStoreOwnerProfileId(profile.id!!)
        if (stores.isEmpty()) {
            throw IllegalStateException("No stores found for store owner")
        }
        return stores.first().id!!
    }
}
