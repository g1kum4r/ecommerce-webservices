package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.WishlistItem
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WishlistRepository : CrudRepository<WishlistItem, UUID> {
    @Query("SELECT * FROM wishlists WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByUserId(userId: UUID, limit: Int, offset: Int): List<WishlistItem>

    @Query("SELECT COUNT(*) FROM wishlists WHERE user_id = :userId")
    fun countByUserId(userId: UUID): Long

    @Query("SELECT * FROM wishlists WHERE user_id = :userId AND product_id = :productId")
    fun findByUserIdAndProductId(userId: UUID, productId: UUID): WishlistItem?

    fun existsByUserIdAndProductId(userId: UUID, productId: UUID): Boolean

    @Query("DELETE FROM wishlists WHERE user_id = :userId AND product_id = :productId")
    fun deleteByUserIdAndProductId(userId: UUID, productId: UUID)
}
