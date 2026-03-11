package lakho.ecommerce.webservices.consumer.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.common.services.UserService
import lakho.ecommerce.webservices.consumer.api.models.CancelOrderRequest
import lakho.ecommerce.webservices.consumer.api.models.CheckoutRequest
import lakho.ecommerce.webservices.consumer.api.models.OrderListResponse
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.services.CheckoutService
import lakho.ecommerce.webservices.consumer.services.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/consumer/orders")
class OrderController(
    private val checkoutService: CheckoutService,
    private val orderService: OrderService,
    private val userService: UserService
) {

    @PostMapping("/checkout")
    fun checkout(
        authentication: Authentication,
        @Valid @RequestBody request: CheckoutRequest
    ): ResponseEntity<List<OrderResponse>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val orders = checkoutService.checkout(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(orders)
    }

    @GetMapping
    fun getOrders(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<CustomPage<OrderListResponse>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val orders = orderService.getOrders(user.id, page, size, status)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{id}")
    fun getOrder(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val order = orderService.getOrderById(user.id, id)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{id}/cancel")
    fun cancelOrder(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestBody(required = false) request: CancelOrderRequest?
    ): ResponseEntity<OrderResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val order = orderService.cancelOrder(user.id, id, request?.reason)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{id}/confirm-delivery")
    fun confirmDelivery(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val order = orderService.confirmDelivery(user.id, id)
        return ResponseEntity.ok(order)
    }
}
