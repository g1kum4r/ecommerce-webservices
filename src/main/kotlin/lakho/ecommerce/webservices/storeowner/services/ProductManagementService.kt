package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.storeowner.api.models.AddProductImageRequest
import lakho.ecommerce.webservices.storeowner.api.models.AddProductVariantRequest
import lakho.ecommerce.webservices.storeowner.api.models.CreateProductRequest
import lakho.ecommerce.webservices.storeowner.api.models.ProductResponse
import lakho.ecommerce.webservices.storeowner.api.models.UpdateProductRequest
import lakho.ecommerce.webservices.storeowner.api.models.UpdateProductVariantRequest
import lakho.ecommerce.webservices.storeowner.repositories.ProductImageRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductVariantRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreOwnerProfileRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreRepository
import lakho.ecommerce.webservices.storeowner.repositories.entities.Product
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductImage
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductVariant
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProductManagementService(
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val storeRepository: StoreRepository,
    private val storeOwnerProfileRepository: StoreOwnerProfileRepository
) {

    @Transactional(readOnly = false)
    fun createProduct(userId: UUID, request: CreateProductRequest): ProductResponse {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val stores = storeRepository.findAllByStoreOwnerProfileId(profile.id!!)
        if (stores.isEmpty()) {
            throw IllegalStateException("No stores found for store owner")
        }
        val store = stores.first()

        val slug = generateUniqueSlug(request.name)

        val product = Product(
            storeId = store.id!!,
            categoryId = request.categoryId,
            name = request.name,
            slug = slug,
            description = request.description,
            shortDescription = request.shortDescription,
            sku = request.sku,
            barcode = request.barcode,
            brand = request.brand,
            basePrice = request.basePrice,
            salePrice = request.salePrice,
            costPrice = request.costPrice,
            currency = request.currency,
            stockQuantity = request.stockQuantity,
            lowStockThreshold = request.lowStockThreshold,
            weight = request.weight,
            weightUnit = request.weightUnit,
            isDigital = request.isDigital,
            isFeatured = request.isFeatured,
            metaTitle = request.metaTitle,
            metaDescription = request.metaDescription,
            tags = request.tags
        )

        val saved = productRepository.save(product)
        return ProductResponse.from(saved, emptyList(), emptyList())
    }

    @Transactional(readOnly = true)
    fun getProducts(userId: UUID, page: Int, size: Int, status: String?, search: String?): CustomPage<ProductResponse> {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val stores = storeRepository.findAllByStoreOwnerProfileId(profile.id!!)
        if (stores.isEmpty()) return CustomPage.of(emptyList(), 0, page, size)

        val storeId = stores.first().id!!
        val offset = page * size

        val (products, total) = when {
            !search.isNullOrBlank() -> {
                val items = productRepository.searchByStoreId(storeId, search, size, offset)
                val count = productRepository.countSearchByStoreId(storeId, search)
                items to count
            }
            !status.isNullOrBlank() -> {
                val items = productRepository.findByStoreIdAndStatus(storeId, status, size, offset)
                val count = productRepository.countByStoreIdAndStatus(storeId, status)
                items to count
            }
            else -> {
                val pageResult = productRepository.findByStoreId(storeId, PageRequest.of(page, size))
                pageResult.content to pageResult.totalElements
            }
        }

        val responses = products.map { product ->
            val images = productImageRepository.findByProductId(product.id!!)
            val variants = productVariantRepository.findByProductId(product.id)
            ProductResponse.from(product, images, variants)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getProductById(userId: UUID, productId: UUID): ProductResponse? {
        val product = productRepository.findById(productId).orElse(null) ?: return null
        verifyOwnership(userId, product)

        val images = productImageRepository.findByProductId(product.id!!)
        val variants = productVariantRepository.findByProductId(product.id)
        return ProductResponse.from(product, images, variants)
    }

    @Transactional(readOnly = false)
    fun updateProduct(userId: UUID, productId: UUID, request: UpdateProductRequest): ProductResponse {
        val existing = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, existing)

        val newSlug = if (request.name != null && request.name != existing.name) {
            generateUniqueSlug(request.name)
        } else {
            existing.slug
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            slug = newSlug,
            description = request.description ?: existing.description,
            shortDescription = request.shortDescription ?: existing.shortDescription,
            categoryId = request.categoryId ?: existing.categoryId,
            sku = request.sku ?: existing.sku,
            barcode = request.barcode ?: existing.barcode,
            brand = request.brand ?: existing.brand,
            basePrice = request.basePrice ?: existing.basePrice,
            salePrice = request.salePrice ?: existing.salePrice,
            costPrice = request.costPrice ?: existing.costPrice,
            currency = request.currency ?: existing.currency,
            stockQuantity = request.stockQuantity ?: existing.stockQuantity,
            lowStockThreshold = request.lowStockThreshold ?: existing.lowStockThreshold,
            weight = request.weight ?: existing.weight,
            weightUnit = request.weightUnit ?: existing.weightUnit,
            isDigital = request.isDigital ?: existing.isDigital,
            isFeatured = request.isFeatured ?: existing.isFeatured,
            metaTitle = request.metaTitle ?: existing.metaTitle,
            metaDescription = request.metaDescription ?: existing.metaDescription,
            tags = request.tags ?: existing.tags,
            updatedAt = Instant.now()
        )

        val saved = productRepository.save(updated)
        val images = productImageRepository.findByProductId(saved.id!!)
        val variants = productVariantRepository.findByProductId(saved.id)
        return ProductResponse.from(saved, images, variants)
    }

    @Transactional(readOnly = false)
    fun deleteProduct(userId: UUID, productId: UUID) {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)
        productRepository.delete(product)
    }

    @Transactional(readOnly = false)
    fun changeProductStatus(userId: UUID, productId: UUID, status: String): ProductResponse {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val validStatuses = listOf("DRAFT", "PUBLISHED", "ARCHIVED")
        if (status !in validStatuses) {
            throw IllegalArgumentException("Invalid status: $status. Must be one of: $validStatuses")
        }

        val updated = product.copy(status = status, updatedAt = Instant.now())
        val saved = productRepository.save(updated)
        val images = productImageRepository.findByProductId(saved.id!!)
        val variants = productVariantRepository.findByProductId(saved.id)
        return ProductResponse.from(saved, images, variants)
    }

    @Transactional(readOnly = false)
    fun addImage(userId: UUID, productId: UUID, request: AddProductImageRequest): ProductImage {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val image = ProductImage(
            productId = productId,
            imageUrl = request.imageUrl,
            altText = request.altText,
            displayOrder = request.displayOrder,
            isPrimary = request.isPrimary
        )
        return productImageRepository.save(image)
    }

    @Transactional(readOnly = false)
    fun removeImage(userId: UUID, productId: UUID, imageId: UUID) {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val image = productImageRepository.findById(imageId).orElseThrow {
            IllegalArgumentException("Image not found: $imageId")
        }
        if (image.productId != productId) {
            throw IllegalArgumentException("Image does not belong to product")
        }
        productImageRepository.delete(image)
    }

    @Transactional(readOnly = false)
    fun addVariant(userId: UUID, productId: UUID, request: AddProductVariantRequest): ProductVariant {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val variant = ProductVariant(
            productId = productId,
            name = request.name,
            sku = request.sku,
            price = request.price,
            stockQuantity = request.stockQuantity,
            attributes = request.attributes
        )
        return productVariantRepository.save(variant)
    }

    @Transactional(readOnly = false)
    fun updateVariant(userId: UUID, productId: UUID, variantId: UUID, request: UpdateProductVariantRequest): ProductVariant {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val existing = productVariantRepository.findById(variantId).orElseThrow {
            IllegalArgumentException("Variant not found: $variantId")
        }
        if (existing.productId != productId) {
            throw IllegalArgumentException("Variant does not belong to product")
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            sku = request.sku ?: existing.sku,
            price = request.price ?: existing.price,
            stockQuantity = request.stockQuantity ?: existing.stockQuantity,
            attributes = request.attributes ?: existing.attributes,
            isActive = request.isActive ?: existing.isActive,
            updatedAt = Instant.now()
        )
        return productVariantRepository.save(updated)
    }

    @Transactional(readOnly = false)
    fun removeVariant(userId: UUID, productId: UUID, variantId: UUID) {
        val product = productRepository.findById(productId).orElseThrow {
            IllegalArgumentException("Product not found: $productId")
        }
        verifyOwnership(userId, product)

        val variant = productVariantRepository.findById(variantId).orElseThrow {
            IllegalArgumentException("Variant not found: $variantId")
        }
        if (variant.productId != productId) {
            throw IllegalArgumentException("Variant does not belong to product")
        }
        productVariantRepository.delete(variant)
    }

    private fun verifyOwnership(userId: UUID, product: Product) {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found")

        val store = storeRepository.findById(product.storeId).orElseThrow {
            IllegalStateException("Store not found: ${product.storeId}")
        }

        if (store.storeOwnerProfileId != profile.id) {
            throw IllegalAccessException("Product does not belong to user's store")
        }
    }

    private fun generateUniqueSlug(name: String): String {
        val baseSlug = name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .take(255)

        if (!productRepository.existsBySlug(baseSlug)) return baseSlug

        var slug: String
        do {
            val suffix = UUID.randomUUID().toString().take(6)
            slug = "${baseSlug}-$suffix".take(300)
        } while (productRepository.existsBySlug(slug))

        return slug
    }
}
