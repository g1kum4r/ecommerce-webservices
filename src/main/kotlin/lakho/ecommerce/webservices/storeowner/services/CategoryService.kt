package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.storeowner.repositories.ProductCategoryRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreCategoryRepository
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductCategory
import lakho.ecommerce.webservices.storeowner.repositories.entities.StoreCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val productCategoryRepository: ProductCategoryRepository,
    private val storeCategoryRepository: StoreCategoryRepository
) {

    // Product Categories
    @Transactional(readOnly = true)
    fun getAllProductCategories(pageable: Pageable): Page<ProductCategory> {
        return productCategoryRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    fun getRootProductCategories(pageable: Pageable): Page<ProductCategory> {
        return productCategoryRepository.findAllRootCategories(pageable)
    }

    @Transactional(readOnly = true)
    fun searchProductCategories(search: String, pageable: Pageable): Page<ProductCategory> {
        return productCategoryRepository.searchByName(search, pageable)
    }

    @Transactional(readOnly = true)
    fun getProductCategoryById(id: Long): ProductCategory? {
        return productCategoryRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun getProductCategoryBySlug(slug: String): ProductCategory? {
        return productCategoryRepository.findBySlug(slug)
    }

    @Transactional(readOnly = true)
    fun getProductCategoryChildren(parentId: Long, pageable: Pageable): Page<ProductCategory> {
        return productCategoryRepository.findByParentId(parentId, pageable)
    }

    // Store Categories
    @Transactional(readOnly = true)
    fun getAllStoreCategories(pageable: Pageable): Page<StoreCategory> {
        return storeCategoryRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    fun getRootStoreCategories(pageable: Pageable): Page<StoreCategory> {
        return storeCategoryRepository.findAllRootCategories(pageable)
    }

    @Transactional(readOnly = true)
    fun searchStoreCategories(search: String, pageable: Pageable): Page<StoreCategory> {
        return storeCategoryRepository.searchByName(search, pageable)
    }

    @Transactional(readOnly = true)
    fun getStoreCategoryById(id: Long): StoreCategory? {
        return storeCategoryRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun getStoreCategoryBySlug(slug: String): StoreCategory? {
        return storeCategoryRepository.findBySlug(slug)
    }

    @Transactional(readOnly = true)
    fun getStoreCategoryChildren(parentId: Long, pageable: Pageable): Page<StoreCategory> {
        return storeCategoryRepository.findByParentId(parentId, pageable)
    }
}
