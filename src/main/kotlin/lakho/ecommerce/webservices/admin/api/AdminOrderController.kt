package lakho.ecommerce.webservices.admin.api

import lakho.ecommerce.webservices.admin.services.AdminOrderService
import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderStatsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val adminOrderService: AdminOrderService
) {

    @GetMapping
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) storeId: UUID?,
        @RequestParam(required = false) userId: UUID?
    ): ResponseEntity<CustomPage<OrderListResponse>> {
        val orders = adminOrderService.getAllOrders(page, size, status, storeId, userId)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): ResponseEntity<OrderResponse> {
        val order = adminOrderService.getOrderById(id)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/stats")
    fun getOrderStats(): ResponseEntity<OrderStatsResponse> {
        val stats = adminOrderService.getOrderStats()
        return ResponseEntity.ok(stats)
    }
}
