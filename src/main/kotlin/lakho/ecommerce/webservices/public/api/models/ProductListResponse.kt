package lakho.ecommerce.webservices.public.api.models

import lakho.ecommerce.webservices.storeowner.repositories.entities.Product
import java.math.BigDecimal
import java.util.UUID

data class ProductListResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val shortDescription: String?,
    val brand: String?,
    val basePrice: BigDecimal,
    val salePrice: BigDecimal?,
    val currency: String,
    val primaryImageUrl: String?,
    val isFeatured: Boolean,
    val categoryId: Long?,
    val storeId: UUID
) {
    companion object {
        fun from(product: Product, primaryImageUrl: String?): ProductListResponse {
            return ProductListResponse(
                id = product.id!!,
                name = product.name,
                slug = product.slug,
                shortDescription = product.shortDescription,
                brand = product.brand,
                basePrice = product.basePrice,
                salePrice = product.salePrice,
                currency = product.currency,
                primaryImageUrl = primaryImageUrl,
                isFeatured = product.isFeatured,
                categoryId = product.categoryId,
                storeId = product.storeId
            )
        }
    }
}
