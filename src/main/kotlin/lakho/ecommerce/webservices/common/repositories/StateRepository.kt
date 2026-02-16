package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.State
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StateRepository : CrudRepository<State, Long> {

    @Query("SELECT * FROM states WHERE is_active = true AND country_id = :countryId LIMIT :limit OFFSET :offset")
    fun findByCountryId(countryId: Long, limit: Int, offset: Long): List<State>

    @Query("SELECT COUNT(*) FROM states WHERE is_active = true AND country_id = :countryId")
    fun countByCountryId(countryId: Long): Long

    @Query("SELECT * FROM states WHERE is_active = true LIMIT :limit OFFSET :offset")
    fun findAllActive(limit: Int, offset: Long): List<State>

    @Query("SELECT COUNT(*) FROM states WHERE is_active = true")
    fun countAllActive(): Long

    @Query("""
        SELECT * FROM states
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR state_code ILIKE CONCAT('%', :search, '%'))
        LIMIT :limit OFFSET :offset
    """)
    fun searchByNameOrCode(search: String, limit: Int, offset: Long): List<State>

    @Query("""
        SELECT COUNT(*) FROM states
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR state_code ILIKE CONCAT('%', :search, '%'))
    """)
    fun countSearchByNameOrCode(search: String): Long
}
