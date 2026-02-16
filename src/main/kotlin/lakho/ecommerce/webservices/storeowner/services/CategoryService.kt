package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.storeowner.repositories.ProductCategoryRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreCategoryRepository
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductCategory
import lakho.ecommerce.webservices.storeowner.repositories.entities.StoreCategory
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
    fun getAllProductCategories(pageable: Pageable): CustomPage<ProductCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = productCategoryRepository.findByParentIdIsNull(pageable.pageSize, offset)
        val totalElements = productCategoryRepository.countByParentIdIsNull()
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    @Transactional(readOnly = true)
    fun getRootProductCategories(pageable: Pageable): CustomPage<ProductCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = productCategoryRepository.findAllRootCategories(pageable.pageSize, offset)
        val totalElements = productCategoryRepository.countAllRootCategories()
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    @Transactional(readOnly = true)
    fun searchProductCategories(search: String, pageable: Pageable): CustomPage<ProductCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = productCategoryRepository.searchByName(search, pageable.pageSize, offset)
        val totalElements = productCategoryRepository.countSearchByName(search)
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
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
    fun getProductCategoryChildren(parentId: Long, pageable: Pageable): CustomPage<ProductCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = productCategoryRepository.findByParentId(parentId, pageable.pageSize, offset)
        val totalElements = productCategoryRepository.countByParentId(parentId)
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    // Store Categories
    @Transactional(readOnly = true)
    fun getAllStoreCategories(pageable: Pageable): CustomPage<StoreCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = storeCategoryRepository.findByParentIdIsNull(pageable.pageSize, offset)
        val totalElements = storeCategoryRepository.countByParentIdIsNull()
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    @Transactional(readOnly = true)
    fun getRootStoreCategories(pageable: Pageable): CustomPage<StoreCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = storeCategoryRepository.findAllRootCategories(pageable.pageSize, offset)
        val totalElements = storeCategoryRepository.countAllRootCategories()
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    @Transactional(readOnly = true)
    fun searchStoreCategories(search: String, pageable: Pageable): CustomPage<StoreCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = storeCategoryRepository.searchByName(search, pageable.pageSize, offset)
        val totalElements = storeCategoryRepository.countSearchByName(search)
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
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
    fun getStoreCategoryChildren(parentId: Long, pageable: Pageable): CustomPage<StoreCategory> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = storeCategoryRepository.findByParentId(parentId, pageable.pageSize, offset)
        val totalElements = storeCategoryRepository.countByParentId(parentId)
        return CustomPage.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }
}
