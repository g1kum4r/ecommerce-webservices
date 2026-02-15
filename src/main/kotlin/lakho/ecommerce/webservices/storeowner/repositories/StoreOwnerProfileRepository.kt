package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.StoreOwnerProfile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StoreOwnerProfileRepository : CrudRepository<StoreOwnerProfile, UUID> {
    fun findByUserId(userId: UUID): StoreOwnerProfile?
    fun existsByUserId(userId: UUID): Boolean
}
