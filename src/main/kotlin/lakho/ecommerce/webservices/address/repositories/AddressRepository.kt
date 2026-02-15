package lakho.ecommerce.webservices.address.repositories

import lakho.ecommerce.webservices.address.repositories.entities.Address
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AddressRepository : CrudRepository<Address, UUID>, PagingAndSortingRepository<Address, UUID> {

    @Query("SELECT * FROM addresses WHERE city_id = :cityId")
    fun findByCityId(cityId: Long, pageable: Pageable): Page<Address>
}
