package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.Cart
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CartRepository : CrudRepository<Cart, UUID> {
    fun findByUserId(userId: UUID): Cart?
}
