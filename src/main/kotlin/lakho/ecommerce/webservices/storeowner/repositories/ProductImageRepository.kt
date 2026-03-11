package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductImage
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductImageRepository : CrudRepository<ProductImage, UUID> {

    @Query("SELECT * FROM product_images WHERE product_id = :productId ORDER BY display_order ASC")
    fun findByProductId(productId: UUID): List<ProductImage>

    @Modifying
    @Query("DELETE FROM product_images WHERE product_id = :productId")
    fun deleteByProductId(productId: UUID)
}
