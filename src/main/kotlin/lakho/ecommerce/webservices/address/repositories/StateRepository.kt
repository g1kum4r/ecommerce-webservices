package lakho.ecommerce.webservices.address.repositories

import lakho.ecommerce.webservices.address.repositories.entities.State
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface StateRepository : CrudRepository<State, Long>, PagingAndSortingRepository<State, Long> {

    @Query("SELECT * FROM states WHERE is_active = true AND country_id = :countryId")
    fun findByCountryId(countryId: Long, pageable: Pageable): Page<State>

    @Query("SELECT * FROM states WHERE is_active = true")
    fun findAllActive(pageable: Pageable): Page<State>

    @Query("""
        SELECT * FROM states
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR state_code ILIKE CONCAT('%', :search, '%'))
    """)
    fun searchByNameOrCode(search: String, pageable: Pageable): Page<State>
}
