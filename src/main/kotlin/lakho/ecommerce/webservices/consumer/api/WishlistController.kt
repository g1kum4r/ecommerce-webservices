package lakho.ecommerce.webservices.consumer.api

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.repositories.entities.WishlistItem
import lakho.ecommerce.webservices.consumer.services.WishlistService
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/consumer/wishlist")
class WishlistController(
    private val wishlistService: WishlistService,
    private val userService: UserService
) {
    @GetMapping
    fun getWishlist(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomPage<WishlistItem>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(wishlistService.getWishlist(user.id, page, size))
    }

    @PostMapping("/{productId}")
    fun addToWishlist(
        authentication: Authentication,
        @PathVariable productId: UUID
    ): ResponseEntity<WishlistItem> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val item = wishlistService.addToWishlist(user.id, productId)
        return ResponseEntity.status(HttpStatus.CREATED).body(item)
    }

    @DeleteMapping("/{productId}")
    fun removeFromWishlist(
        authentication: Authentication,
        @PathVariable productId: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        wishlistService.removeFromWishlist(user.id, productId)
        return ResponseEntity.noContent().build()
    }
}
