package lakho.ecommerce.webservices.storeowner.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.common.services.UserService
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderStatsResponse
import lakho.ecommerce.webservices.consumer.api.models.UpdateOrderStatusRequest
import lakho.ecommerce.webservices.storeowner.services.OrderManagementService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/storeowner/orders")
class OrderManagementController(
    private val orderManagementService: OrderManagementService,
    private val userService: UserService
) {

    @GetMapping
    fun getStoreOrders(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<CustomPage<OrderListResponse>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val orders = orderManagementService.getStoreOrders(user.id, page, size, status)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/stats")
    fun getOrderStats(
        authentication: Authentication
    ): ResponseEntity<OrderStatsResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val stats = orderManagementService.getOrderStats(user.id)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{id}")
    fun getStoreOrder(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val order = orderManagementService.getStoreOrderById(user.id, id)
        return ResponseEntity.ok(order)
    }

    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val order = orderManagementService.updateOrderStatus(user.id, id, request.status)
        return ResponseEntity.ok(order)
    }
}
