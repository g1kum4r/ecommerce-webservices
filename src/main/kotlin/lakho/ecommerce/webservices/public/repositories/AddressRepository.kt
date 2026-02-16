package lakho.ecommerce.webservices.public.repositories

import lakho.ecommerce.webservices.public.repositories.entities.Address
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AddressRepository : CrudRepository<Address, UUID> {

    @Query("SELECT * FROM addresses WHERE city_id = :cityId LIMIT :limit OFFSET :offset")
    fun findByCityId(cityId: Long, limit: Int, offset: Long): List<Address>

    @Query("SELECT COUNT(*) FROM addresses WHERE city_id = :cityId")
    fun countByCityId(cityId: Long): Long
}
