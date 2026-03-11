package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : CrudRepository<Product, UUID>, PagingAndSortingRepository<Product, UUID> {

    fun findByStoreId(storeId: UUID, pageable: Pageable): Page<Product>

    fun findBySlug(slug: String): Product?

    fun existsBySlug(slug: String): Boolean

    @Query("SELECT * FROM products WHERE store_id = :storeId AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByStoreIdAndStatus(storeId: UUID, status: String, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE store_id = :storeId AND status = :status")
    fun countByStoreIdAndStatus(storeId: UUID, status: String): Long

    @Query("SELECT * FROM products WHERE store_id = :storeId AND (LOWER(name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun searchByStoreId(storeId: UUID, search: String, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE store_id = :storeId AND (LOWER(name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')))")
    fun countSearchByStoreId(storeId: UUID, search: String): Long

    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' AND is_active = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findPublished(limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'PUBLISHED' AND is_active = true")
    fun countPublished(): Long

    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' AND is_active = true AND is_featured = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findFeatured(limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'PUBLISHED' AND is_active = true AND is_featured = true")
    fun countFeatured(): Long

    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' AND is_active = true AND category_id = :categoryId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findPublishedByCategory(categoryId: Long, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'PUBLISHED' AND is_active = true AND category_id = :categoryId")
    fun countPublishedByCategory(categoryId: Long): Long

    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' AND is_active = true AND store_id = :storeId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findPublishedByStoreId(storeId: UUID, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'PUBLISHED' AND is_active = true AND store_id = :storeId")
    fun countPublishedByStoreId(storeId: UUID): Long

    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' AND is_active = true AND (LOWER(name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun searchPublished(search: String, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'PUBLISHED' AND is_active = true AND (LOWER(name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')))")
    fun countSearchPublished(search: String): Long

    @Query("SELECT * FROM products WHERE store_id IN (SELECT id FROM stores WHERE slug = :storeSlug) AND status = 'PUBLISHED' AND is_active = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findPublishedByStoreSlug(storeSlug: String, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE store_id IN (SELECT id FROM stores WHERE slug = :storeSlug) AND status = 'PUBLISHED' AND is_active = true")
    fun countPublishedByStoreSlug(storeSlug: String): Long
}
