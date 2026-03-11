package lakho.ecommerce.webservices.consumer.api.models

import java.math.BigDecimal
import java.util.UUID

data class CartResponse(
    val id: UUID,
    val items: List<CartItemResponse>,
    val subtotal: BigDecimal,
    val itemCount: Int
)

data class CartItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val productSlug: String,
    val productImageUrl: String?,
    val variantId: UUID?,
    val variantName: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val availableStock: Int
)
