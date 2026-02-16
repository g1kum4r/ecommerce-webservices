package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.City
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CityRepository : CrudRepository<City, Long> {

    @Query("SELECT * FROM cities WHERE is_active = true AND state_id = :stateId LIMIT :limit OFFSET :offset")
    fun findByStateId(stateId: Long, limit: Int, offset: Long): List<City>

    @Query("SELECT COUNT(*) FROM cities WHERE is_active = true AND state_id = :stateId")
    fun countByStateId(stateId: Long): Long

    @Query("SELECT * FROM cities WHERE is_active = true LIMIT :limit OFFSET :offset")
    fun findAllActive(limit: Int, offset: Long): List<City>

    @Query("SELECT COUNT(*) FROM cities WHERE is_active = true")
    fun countAllActive(): Long

    @Query("SELECT * FROM cities WHERE is_active = true AND name ILIKE CONCAT('%', :search, '%') LIMIT :limit OFFSET :offset")
    fun searchByName(search: String, limit: Int, offset: Long): List<City>

    @Query("SELECT COUNT(*) FROM cities WHERE is_active = true AND name ILIKE CONCAT('%', :search, '%')")
    fun countSearchByName(search: String): Long
}
