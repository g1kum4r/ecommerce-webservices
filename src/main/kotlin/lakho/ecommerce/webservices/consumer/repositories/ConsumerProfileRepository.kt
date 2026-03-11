package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.ConsumerProfile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConsumerProfileRepository : CrudRepository<ConsumerProfile, UUID> {
    fun findByUserId(userId: UUID): ConsumerProfile?
    fun existsByUserId(userId: UUID): Boolean
}
