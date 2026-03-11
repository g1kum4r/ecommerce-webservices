package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.ConsumerAddress
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConsumerAddressRepository : CrudRepository<ConsumerAddress, UUID> {
    @Query("SELECT * FROM consumer_addresses WHERE user_id = :userId ORDER BY is_default DESC, created_at DESC")
    fun findByUserId(userId: UUID): List<ConsumerAddress>

    @Modifying
    @Query("UPDATE consumer_addresses SET is_default = false WHERE user_id = :userId")
    fun clearDefaultForUser(userId: UUID)
}
