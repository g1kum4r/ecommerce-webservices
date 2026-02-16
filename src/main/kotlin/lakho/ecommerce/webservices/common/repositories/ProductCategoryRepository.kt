package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.ProductCategory
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductCategoryRepository : CrudRepository<ProductCategory, Long> {
    fun findBySlug(slug: String): ProductCategory?

    @Query("SELECT * FROM products_categories WHERE parent_id IS NULL LIMIT :limit OFFSET :offset")
    fun findByParentIdIsNull(limit: Int, offset: Long): List<ProductCategory>

    @Query("SELECT COUNT(*) FROM products_categories WHERE parent_id IS NULL")
    fun countByParentIdIsNull(): Long

    @Query("SELECT * FROM products_categories WHERE parent_id = :parentId LIMIT :limit OFFSET :offset")
    fun findByParentId(parentId: Long, limit: Int, offset: Long): List<ProductCategory>

    @Query("SELECT COUNT(*) FROM products_categories WHERE parent_id = :parentId")
    fun countByParentId(parentId: Long): Long

    @Query("SELECT * FROM products_categories WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR description ILIKE CONCAT('%', :search, '%')) LIMIT :limit OFFSET :offset")
    fun searchByName(search: String, limit: Int, offset: Long): List<ProductCategory>

    @Query("SELECT COUNT(*) FROM products_categories WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR description ILIKE CONCAT('%', :search, '%'))")
    fun countSearchByName(search: String): Long

    @Query("SELECT * FROM products_categories WHERE is_active = true AND parent_id IS NULL LIMIT :limit OFFSET :offset")
    fun findAllRootCategories(limit: Int, offset: Long): List<ProductCategory>

    @Query("SELECT COUNT(*) FROM products_categories WHERE is_active = true AND parent_id IS NULL")
    fun countAllRootCategories(): Long
}
