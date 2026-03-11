package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.consumer.api.models.AddToCartRequest
import lakho.ecommerce.webservices.consumer.api.models.CartItemResponse
import lakho.ecommerce.webservices.consumer.api.models.CartResponse
import lakho.ecommerce.webservices.consumer.api.models.UpdateCartItemRequest
import lakho.ecommerce.webservices.consumer.repositories.CartItemRepository
import lakho.ecommerce.webservices.consumer.repositories.CartRepository
import lakho.ecommerce.webservices.consumer.repositories.entities.Cart
import lakho.ecommerce.webservices.consumer.repositories.entities.CartItem
import lakho.ecommerce.webservices.storeowner.repositories.ProductImageRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVariantRepository: ProductVariantRepository
) {

    @Transactional(readOnly = true)
    fun getCart(userId: UUID): CartResponse {
        val cart = getOrCreateCart(userId)
        return buildCartResponse(cart)
    }

    @Transactional(readOnly = false)
    fun addItem(userId: UUID, request: AddToCartRequest): CartResponse {
        val cart = getOrCreateCart(userId)

        val product = productRepository.findById(request.productId).orElseThrow {
            IllegalArgumentException("Product not found: ${request.productId}")
        }

        if (product.status != "PUBLISHED" || !product.isActive) {
            throw IllegalArgumentException("Product is not available")
        }

        val availableStock = if (request.variantId != null) {
            val variant = productVariantRepository.findById(request.variantId).orElseThrow {
                IllegalArgumentException("Variant not found: ${request.variantId}")
            }
            variant.stockQuantity
        } else {
            product.stockQuantity
        }

        // Check for existing item
        val existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
            cart.id!!, request.productId, request.variantId
        )

        val totalQuantity = (existingItem?.quantity ?: 0) + request.quantity
        if (totalQuantity > availableStock) {
            throw IllegalArgumentException("Not enough stock. Available: $availableStock")
        }

        val unitPrice = if (request.variantId != null) {
            productVariantRepository.findById(request.variantId).get().price
        } else {
            product.salePrice ?: product.basePrice
        }

        if (existingItem != null) {
            val updated = existingItem.copy(
                quantity = totalQuantity,
                unitPrice = unitPrice,
                updatedAt = Instant.now()
            )
            cartItemRepository.save(updated)
        } else {
            val item = CartItem(
                cartId = cart.id,
                productId = request.productId,
                variantId = request.variantId,
                quantity = request.quantity,
                unitPrice = unitPrice
            )
            cartItemRepository.save(item)
        }

        return buildCartResponse(cart)
    }

    @Transactional(readOnly = false)
    fun updateItem(userId: UUID, itemId: UUID, request: UpdateCartItemRequest): CartResponse {
        val cart = getOrCreateCart(userId)
        val item = cartItemRepository.findById(itemId).orElseThrow {
            IllegalArgumentException("Cart item not found: $itemId")
        }
        if (item.cartId != cart.id) {
            throw IllegalAccessException("Cart item does not belong to user")
        }

        val product = productRepository.findById(item.productId).orElseThrow {
            IllegalArgumentException("Product not found")
        }
        val availableStock = if (item.variantId != null) {
            productVariantRepository.findById(item.variantId).map { it.stockQuantity }.orElse(0)
        } else {
            product.stockQuantity
        }

        if (request.quantity > availableStock) {
            throw IllegalArgumentException("Not enough stock. Available: $availableStock")
        }

        val updated = item.copy(quantity = request.quantity, updatedAt = Instant.now())
        cartItemRepository.save(updated)

        return buildCartResponse(cart)
    }

    @Transactional(readOnly = false)
    fun removeItem(userId: UUID, itemId: UUID): CartResponse {
        val cart = getOrCreateCart(userId)
        val item = cartItemRepository.findById(itemId).orElseThrow {
            IllegalArgumentException("Cart item not found: $itemId")
        }
        if (item.cartId != cart.id) {
            throw IllegalAccessException("Cart item does not belong to user")
        }
        cartItemRepository.delete(item)
        return buildCartResponse(cart)
    }

    @Transactional(readOnly = false)
    fun clearCart(userId: UUID) {
        val cart = cartRepository.findByUserId(userId) ?: return
        cartItemRepository.deleteByCartId(cart.id!!)
    }

    private fun getOrCreateCart(userId: UUID): Cart {
        return cartRepository.findByUserId(userId) ?: cartRepository.save(Cart(userId = userId))
    }

    private fun buildCartResponse(cart: Cart): CartResponse {
        val items = cartItemRepository.findByCartId(cart.id!!)

        val itemResponses = items.map { item ->
            val product = productRepository.findById(item.productId).orElse(null)
            val variant = item.variantId?.let { productVariantRepository.findById(it).orElse(null) }
            val primaryImage = productImageRepository.findByProductId(item.productId)
                .firstOrNull { it.isPrimary }?.imageUrl
                ?: productImageRepository.findByProductId(item.productId).firstOrNull()?.imageUrl

            val availableStock = variant?.stockQuantity ?: product?.stockQuantity ?: 0

            CartItemResponse(
                id = item.id!!,
                productId = item.productId,
                productName = product?.name ?: "Unknown",
                productSlug = product?.slug ?: "",
                productImageUrl = primaryImage,
                variantId = item.variantId,
                variantName = variant?.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.unitPrice.multiply(BigDecimal(item.quantity)),
                availableStock = availableStock
            )
        }

        val subtotal = itemResponses.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalPrice) }

        return CartResponse(
            id = cart.id,
            items = itemResponses,
            subtotal = subtotal,
            itemCount = itemResponses.sumOf { it.quantity }
        )
    }
}
