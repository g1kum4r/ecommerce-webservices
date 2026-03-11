package lakho.ecommerce.webservices.public.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.public.api.models.ProductDetailResponse
import lakho.ecommerce.webservices.public.api.models.ProductListResponse
import lakho.ecommerce.webservices.storeowner.repositories.ProductImageRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductRepository
import lakho.ecommerce.webservices.storeowner.repositories.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PublicProductService(
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVariantRepository: ProductVariantRepository
) {

    @Transactional(readOnly = true)
    fun getProducts(page: Int, size: Int, categoryId: Long?, storeId: UUID?, search: String?): CustomPage<ProductListResponse> {
        val offset = page * size

        val (products, total) = when {
            !search.isNullOrBlank() -> {
                productRepository.searchPublished(search, size, offset) to productRepository.countSearchPublished(search)
            }
            categoryId != null -> {
                productRepository.findPublishedByCategory(categoryId, size, offset) to productRepository.countPublishedByCategory(categoryId)
            }
            storeId != null -> {
                productRepository.findPublishedByStoreId(storeId, size, offset) to productRepository.countPublishedByStoreId(storeId)
            }
            else -> {
                productRepository.findPublished(size, offset) to productRepository.countPublished()
            }
        }

        val responses = products.map { product ->
            val primaryImage = productImageRepository.findByProductId(product.id!!)
                .firstOrNull { it.isPrimary }
                ?.imageUrl
                ?: productImageRepository.findByProductId(product.id).firstOrNull()?.imageUrl
            ProductListResponse.from(product, primaryImage)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getProductBySlug(slug: String): ProductDetailResponse? {
        val product = productRepository.findBySlug(slug) ?: return null
        if (product.status != "PUBLISHED" || !product.isActive) return null

        val images = productImageRepository.findByProductId(product.id!!)
        val variants = productVariantRepository.findByProductId(product.id)
        return ProductDetailResponse.from(product, images, variants)
    }

    @Transactional(readOnly = true)
    fun getProductsByStore(storeSlug: String, page: Int, size: Int): CustomPage<ProductListResponse> {
        val offset = page * size
        val products = productRepository.findPublishedByStoreSlug(storeSlug, size, offset)
        val total = productRepository.countPublishedByStoreSlug(storeSlug)

        val responses = products.map { product ->
            val primaryImage = productImageRepository.findByProductId(product.id!!)
                .firstOrNull { it.isPrimary }?.imageUrl
            ProductListResponse.from(product, primaryImage)
        }

        return CustomPage.of(responses, total, page, size)
    }

    @Transactional(readOnly = true)
    fun getFeaturedProducts(page: Int, size: Int): CustomPage<ProductListResponse> {
        val offset = page * size
        val products = productRepository.findFeatured(size, offset)
        val total = productRepository.countFeatured()

        val responses = products.map { product ->
            val primaryImage = productImageRepository.findByProductId(product.id!!)
                .firstOrNull { it.isPrimary }?.imageUrl
            ProductListResponse.from(product, primaryImage)
        }

        return CustomPage.of(responses, total, page, size)
    }
}
