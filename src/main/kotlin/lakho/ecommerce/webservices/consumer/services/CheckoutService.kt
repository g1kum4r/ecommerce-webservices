package lakho.ecommerce.webservices.consumer.services

import com.fasterxml.jackson.databind.ObjectMapper
import lakho.ecommerce.webservices.consumer.api.models.CheckoutRequest
import lakho.ecommerce.webservices.consumer.api.models.OrderResponse
import lakho.ecommerce.webservices.consumer.repositories.CartItemRepository
import lakho.ecommerce.webservices.consumer.repositories.CartRepository
import lakho.ecommerce.webservices.consumer.repositories.ConsumerAddressRepository
import lakho.ecommerce.webservices.consumer.repositories.OrderItemRepository
import lakho.ecommerce.webservices.consumer.repositories.OrderRepository
import lakho.ecommerce.webservices.consumer.repositories.entities.Order
import lakho.ecommerce.webservices.consumer.repositories.entities.OrderItem
import lakho.ecommerce.webservices.storeowner.repositories.ProductImageRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CheckoutService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val productImageRepository: ProductImageRepository,
    private val consumerAddressRepository: ConsumerAddressRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional(readOnly = false)
    fun checkout(userId: UUID, request: CheckoutRequest): List<OrderResponse> {
        val cart = cartRepository.findByUserId(userId)
            ?: throw IllegalStateException("Cart not found")

        val cartItems = cartItemRepository.findByCartId(cart.id!!)
        if (cartItems.isEmpty()) {
            throw IllegalArgumentException("Cart is empty")
        }

        val address = consumerAddressRepository.findById(request.addressId).orElseThrow {
            IllegalArgumentException("Address not found: ${request.addressId}")
        }
        if (address.userId != userId) {
            throw IllegalAccessException("Address does not belong to user")
        }

        val addressSnapshot = objectMapper.writeValueAsString(
            mapOf(
                "label" to address.label,
                "recipientName" to address.recipientName,
                "phone" to address.phone,
                "addressLine1" to address.addressLine1,
                "addressLine2" to address.addressLine2,
                "cityId" to address.cityId,
                "stateId" to address.stateId,
                "countryId" to address.countryId,
                "postalCode" to address.postalCode
            )
        )

        // Validate all products and group by store
        data class CartItemWithProduct(
            val cartItemId: UUID,
            val productId: UUID,
            val variantId: UUID?,
            val storeId: UUID,
            val productName: String,
            val variantName: String?,
            val productImageUrl: String?,
            val sku: String?,
            val quantity: Int,
            val unitPrice: BigDecimal
        )

        val itemsWithProducts = cartItems.map { cartItem ->
            val product = productRepository.findById(cartItem.productId).orElseThrow {
                IllegalArgumentException("Product not found: ${cartItem.productId}")
            }

            if (product.status != "PUBLISHED" || !product.isActive) {
                throw IllegalArgumentException("Product '${product.name}' is not available for purchase")
            }

            val variant = cartItem.variantId?.let {
                productVariantRepository.findById(it).orElseThrow {
                    IllegalArgumentException("Variant not found: $it")
                }
            }

            val availableStock = variant?.stockQuantity ?: product.stockQuantity
            if (cartItem.quantity > availableStock) {
                throw IllegalArgumentException("Not enough stock for '${product.name}'. Available: $availableStock, Requested: ${cartItem.quantity}")
            }

            val unitPrice = variant?.price ?: (product.salePrice ?: product.basePrice)

            val primaryImage = productImageRepository.findByProductId(product.id!!)
                .firstOrNull { it.isPrimary }?.imageUrl
                ?: productImageRepository.findByProductId(product.id).firstOrNull()?.imageUrl

            CartItemWithProduct(
                cartItemId = cartItem.id!!,
                productId = product.id,
                variantId = cartItem.variantId,
                storeId = product.storeId,
                productName = product.name,
                variantName = variant?.name,
                productImageUrl = primaryImage,
                sku = variant?.sku ?: product.sku,
                quantity = cartItem.quantity,
                unitPrice = unitPrice
            )
        }

        val groupedByStore = itemsWithProducts.groupBy { it.storeId }

        val orderResponses = mutableListOf<OrderResponse>()

        for ((storeId, storeItems) in groupedByStore) {
            val orderNumber = generateOrderNumber()
            val subtotal = storeItems.fold(BigDecimal.ZERO) { acc, item ->
                acc.add(item.unitPrice.multiply(BigDecimal(item.quantity)))
            }

            val order = Order(
                orderNumber = orderNumber,
                userId = userId,
                storeId = storeId,
                shippingAddressSnapshot = addressSnapshot,
                subtotal = subtotal,
                totalAmount = subtotal,
                currency = "USD",
                status = "PENDING",
                paymentStatus = "UNPAID",
                paymentMethod = request.paymentMethod,
                notes = request.notes
            )

            val savedOrder = orderRepository.save(order)

            val orderItems = storeItems.map { item ->
                val orderItem = OrderItem(
                    orderId = savedOrder.id!!,
                    productId = item.productId,
                    variantId = item.variantId,
                    productName = item.productName,
                    variantName = item.variantName,
                    productImageUrl = item.productImageUrl,
                    sku = item.sku,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.unitPrice.multiply(BigDecimal(item.quantity))
                )
                orderItemRepository.save(orderItem)
            }

            // Deduct stock
            for (item in storeItems) {
                if (item.variantId != null) {
                    val variant = productVariantRepository.findById(item.variantId).get()
                    productVariantRepository.save(
                        variant.copy(
                            stockQuantity = variant.stockQuantity - item.quantity,
                            updatedAt = Instant.now()
                        )
                    )
                } else {
                    val product = productRepository.findById(item.productId).get()
                    productRepository.save(
                        product.copy(
                            stockQuantity = product.stockQuantity - item.quantity,
                            updatedAt = Instant.now()
                        )
                    )
                }
            }

            orderResponses.add(OrderResponse.from(savedOrder, orderItems))
        }

        // Clear cart after successful checkout
        cartItemRepository.deleteByCartId(cart.id)

        return orderResponses
    }

    private fun generateOrderNumber(): String {
        val datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val randomPart = buildString {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            repeat(4) {
                append(chars.random())
            }
        }
        val orderNumber = "ORD-$datePart-$randomPart"
        // Ensure uniqueness
        return if (orderRepository.findByOrderNumber(orderNumber) == null) {
            orderNumber
        } else {
            generateOrderNumber()
        }
    }
}
