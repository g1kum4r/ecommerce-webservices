package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.CartItem
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CartItemRepository : CrudRepository<CartItem, UUID> {
    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId ORDER BY created_at ASC")
    fun findByCartId(cartId: UUID): List<CartItem>

    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    fun deleteByCartId(cartId: UUID)

    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId AND product_id = :productId AND (variant_id = :variantId OR (variant_id IS NULL AND :variantId IS NULL))")
    fun findByCartIdAndProductIdAndVariantId(cartId: UUID, productId: UUID, variantId: UUID?): CartItem?
}
