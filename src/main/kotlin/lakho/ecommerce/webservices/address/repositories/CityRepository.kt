package lakho.ecommerce.webservices.address.repositories

import lakho.ecommerce.webservices.address.repositories.entities.City
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CityRepository : CrudRepository<City, Long>, PagingAndSortingRepository<City, Long> {

    @Query("SELECT * FROM cities WHERE is_active = true AND state_id = :stateId")
    fun findByStateId(stateId: Long, pageable: Pageable): Page<City>

    @Query("SELECT * FROM cities WHERE is_active = true")
    fun findAllActive(pageable: Pageable): Page<City>

    @Query("SELECT * FROM cities WHERE is_active = true AND name ILIKE CONCAT('%', :search, '%')")
    fun searchByName(search: String, pageable: Pageable): Page<City>
}
