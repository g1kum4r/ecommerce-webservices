package lakho.ecommerce.webservices.consumer.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.consumer.api.models.AddToCartRequest
import lakho.ecommerce.webservices.consumer.api.models.CartResponse
import lakho.ecommerce.webservices.consumer.api.models.UpdateCartItemRequest
import lakho.ecommerce.webservices.consumer.services.CartService
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/consumer/cart")
class CartController(
    private val cartService: CartService,
    private val userService: UserService
) {
    @GetMapping
    fun getCart(authentication: Authentication): ResponseEntity<CartResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(cartService.getCart(user.id))
    }

    @PostMapping("/items")
    fun addItem(
        authentication: Authentication,
        @Valid @RequestBody request: AddToCartRequest
    ): ResponseEntity<CartResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(cartService.addItem(user.id, request))
    }

    @PutMapping("/items/{itemId}")
    fun updateItem(
        authentication: Authentication,
        @PathVariable itemId: UUID,
        @Valid @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(cartService.updateItem(user.id, itemId, request))
    }

    @DeleteMapping("/items/{itemId}")
    fun removeItem(
        authentication: Authentication,
        @PathVariable itemId: UUID
    ): ResponseEntity<CartResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(cartService.removeItem(user.id, itemId))
    }

    @DeleteMapping
    fun clearCart(authentication: Authentication): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        cartService.clearCart(user.id)
        return ResponseEntity.noContent().build()
    }
}
