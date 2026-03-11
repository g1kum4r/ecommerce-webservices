package lakho.ecommerce.webservices.consumer.api.models

import lakho.ecommerce.webservices.consumer.repositories.entities.OrderItem
import java.math.BigDecimal
import java.util.UUID

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val variantId: UUID?,
    val productName: String,
    val variantName: String?,
    val productImageUrl: String?,
    val sku: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
) {
    companion object {
        fun from(item: OrderItem): OrderItemResponse {
            return OrderItemResponse(
                id = item.id!!,
                productId = item.productId,
                variantId = item.variantId,
                productName = item.productName,
                variantName = item.variantName,
                productImageUrl = item.productImageUrl,
                sku = item.sku,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice
            )
        }
    }
}
