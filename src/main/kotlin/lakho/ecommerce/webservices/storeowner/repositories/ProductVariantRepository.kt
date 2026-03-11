package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductVariant
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductVariantRepository : CrudRepository<ProductVariant, UUID> {

    @Query("SELECT * FROM product_variants WHERE product_id = :productId ORDER BY created_at ASC")
    fun findByProductId(productId: UUID): List<ProductVariant>

    @Modifying
    @Query("DELETE FROM product_variants WHERE product_id = :productId")
    fun deleteByProductId(productId: UUID)
}
