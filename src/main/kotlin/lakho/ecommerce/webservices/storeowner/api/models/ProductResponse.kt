package lakho.ecommerce.webservices.storeowner.api.models

import lakho.ecommerce.webservices.storeowner.repositories.entities.Product
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductImage
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductVariant
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val storeId: UUID,
    val categoryId: Long?,
    val name: String,
    val slug: String,
    val description: String?,
    val shortDescription: String?,
    val sku: String?,
    val barcode: String?,
    val brand: String?,
    val basePrice: BigDecimal,
    val salePrice: BigDecimal?,
    val costPrice: BigDecimal?,
    val currency: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int,
    val weight: BigDecimal?,
    val weightUnit: String,
    val isActive: Boolean,
    val isFeatured: Boolean,
    val isDigital: Boolean,
    val status: String,
    val metaTitle: String?,
    val metaDescription: String?,
    val tags: String?,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(product: Product, images: List<ProductImage>, variants: List<ProductVariant>): ProductResponse {
            return ProductResponse(
                id = product.id!!,
                storeId = product.storeId,
                categoryId = product.categoryId,
                name = product.name,
                slug = product.slug,
                description = product.description,
                shortDescription = product.shortDescription,
                sku = product.sku,
                barcode = product.barcode,
                brand = product.brand,
                basePrice = product.basePrice,
                salePrice = product.salePrice,
                costPrice = product.costPrice,
                currency = product.currency,
                stockQuantity = product.stockQuantity,
                lowStockThreshold = product.lowStockThreshold,
                weight = product.weight,
                weightUnit = product.weightUnit,
                isActive = product.isActive,
                isFeatured = product.isFeatured,
                isDigital = product.isDigital,
                status = product.status,
                metaTitle = product.metaTitle,
                metaDescription = product.metaDescription,
                tags = product.tags,
                images = images,
                variants = variants,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}
