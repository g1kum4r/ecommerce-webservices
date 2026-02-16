package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.Region
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RegionRepository : CrudRepository<Region, Long> {

    fun findByCode(code: String): Region?

    @Query("SELECT * FROM regions WHERE is_active = true LIMIT :limit OFFSET :offset")
    fun findAllActive(limit: Int, offset: Long): List<Region>

    @Query("SELECT COUNT(*) FROM regions WHERE is_active = true")
    fun countAllActive(): Long

    @Query("SELECT * FROM regions WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR code ILIKE CONCAT('%', :search, '%')) LIMIT :limit OFFSET :offset")
    fun searchByNameOrCode(search: String, limit: Int, offset: Long): List<Region>

    @Query("SELECT COUNT(*) FROM regions WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR code ILIKE CONCAT('%', :search, '%'))")
    fun countSearchByNameOrCode(search: String): Long
}
