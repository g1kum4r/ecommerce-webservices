package lakho.ecommerce.webservices.public.api.models

import lakho.ecommerce.webservices.storeowner.repositories.entities.Product
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductImage
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductVariant
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ProductDetailResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val shortDescription: String?,
    val brand: String?,
    val basePrice: BigDecimal,
    val salePrice: BigDecimal?,
    val currency: String,
    val stockQuantity: Int,
    val isFeatured: Boolean,
    val isDigital: Boolean,
    val categoryId: Long?,
    val storeId: UUID,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val tags: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(product: Product, images: List<ProductImage>, variants: List<ProductVariant>): ProductDetailResponse {
            return ProductDetailResponse(
                id = product.id!!,
                name = product.name,
                slug = product.slug,
                description = product.description,
                shortDescription = product.shortDescription,
                brand = product.brand,
                basePrice = product.basePrice,
                salePrice = product.salePrice,
                currency = product.currency,
                stockQuantity = product.stockQuantity,
                isFeatured = product.isFeatured,
                isDigital = product.isDigital,
                categoryId = product.categoryId,
                storeId = product.storeId,
                images = images,
                variants = variants,
                tags = product.tags,
                createdAt = product.createdAt
            )
        }
    }
}
