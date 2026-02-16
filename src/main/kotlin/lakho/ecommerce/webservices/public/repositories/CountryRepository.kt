package lakho.ecommerce.webservices.public.repositories

import lakho.ecommerce.webservices.public.repositories.entities.Country
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CountryRepository : CrudRepository<Country, Long> {

    fun findByIso2(iso2: String): Country?

    fun findByIso3(iso3: String): Country?

    @Query("SELECT * FROM countries WHERE is_active = true AND region_id = :regionId LIMIT :limit OFFSET :offset")
    fun findByRegionId(regionId: Long, limit: Int, offset: Long): List<Country>

    @Query("SELECT COUNT(*) FROM countries WHERE is_active = true AND region_id = :regionId")
    fun countByRegionId(regionId: Long): Long

    @Query("SELECT * FROM countries WHERE is_active = true LIMIT :limit OFFSET :offset")
    fun findAllActive(limit: Int, offset: Long): List<Country>

    @Query("SELECT COUNT(*) FROM countries WHERE is_active = true")
    fun countAllActive(): Long

    @Query("""
        SELECT * FROM countries
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR iso2 ILIKE CONCAT('%', :search, '%')
             OR iso3 ILIKE CONCAT('%', :search, '%'))
        LIMIT :limit OFFSET :offset
    """)
    fun searchByNameOrCode(search: String, limit: Int, offset: Long): List<Country>

    @Query("""
        SELECT COUNT(*) FROM countries
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR iso2 ILIKE CONCAT('%', :search, '%')
             OR iso3 ILIKE CONCAT('%', :search, '%'))
    """)
    fun countSearchByNameOrCode(search: String): Long
}
