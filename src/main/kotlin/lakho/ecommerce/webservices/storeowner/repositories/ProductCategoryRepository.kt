package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductCategoryRepository : CrudRepository<ProductCategory, Long>, PagingAndSortingRepository<ProductCategory, Long> {
    fun findBySlug(slug: String): ProductCategory?
    fun findByParentIdIsNull(pageable: Pageable): Page<ProductCategory>
    fun findByParentId(parentId: Long, pageable: Pageable): Page<ProductCategory>

    @Query("SELECT * FROM products_categories WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR description ILIKE CONCAT('%', :search, '%'))")
    fun searchByName(search: String, pageable: Pageable): Page<ProductCategory>

    @Query("SELECT * FROM products_categories WHERE is_active = true AND parent_id IS NULL")
    fun findAllRootCategories(pageable: Pageable): Page<ProductCategory>
}
