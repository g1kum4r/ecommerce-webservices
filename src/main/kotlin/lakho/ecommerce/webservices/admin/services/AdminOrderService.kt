package lakho.ecommerce.webservices.admin.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderStatsResponse
import lakho.ecommerce.webservices.consumer.repositories.OrderItemRepository
import lakho.ecommerce.webservices.consumer.repositories.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminOrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    @Transactional(readOnly = true)
    fun getAllOrders(
        page: Int,
        size: Int,
        status: String?,
        storeId: UUID?,
        userId: UUID?
    ): CustomPage<OrderListResponse> {
        val offset = page * size

        val (orders, total) = when {
            storeId != null && !status.isNullOrBlank() -> {
                val items = orderRepository.findAllByStoreIdAndStatus(storeId, status, size, offset)
                val count = orderRepository.countAllByStoreIdAndStatus(storeId, status)
                items to count
            }
            storeId != null -> {
                val items = orderRepository.findAllByStoreId(storeId, size, offset)
                val count = orderRepository.countAllByStoreId(storeId)
                items to count
            }
            userId != null -> {
                val items = orderRepository.findAllByUserId(userId, size, offset)
                val count = orderRepository.countAllByUserId(userId)
                items to count
            }
            !status.isNullOrBlank() -> {
                val items = orderRepository.findAllByStatus(status, size, offset)
                val count = orderRepository.countAllByStatus(status)
                items to count
            }
            else -> {
                val items = orderRepository.findAllOrders(size, offset)
                val count = orderRepository.countAllOrders()
                items to count
            }
        }

        val responses = orders.map { order ->
            val itemCount = orderItemRepository.findByOrderId(order.id!!).sumOf { it.quantity }
            OrderListResponse.from(order, itemCount)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getOrderById(orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow {
            IllegalArgumentException("Order not found: $orderId")
        }
        val items = orderItemRepository.findByOrderId(order.id!!)
        return OrderResponse.from(order, items)
    }

    @Transactional(readOnly = true)
    fun getOrderStats(): OrderStatsResponse {
        val pending = orderRepository.countAllPending()
        val confirmed = orderRepository.countAllConfirmed()
        val processing = orderRepository.countAllProcessing()
        val shipped = orderRepository.countAllShipped()
        val delivered = orderRepository.countAllDelivered()
        val completed = orderRepository.countAllCompleted()
        val cancelled = orderRepository.countAllCancelled()
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
}
