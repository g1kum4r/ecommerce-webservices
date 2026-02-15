package lakho.ecommerce.webservices.address.repositories

import lakho.ecommerce.webservices.address.repositories.entities.Country
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CountryRepository : CrudRepository<Country, Long>, PagingAndSortingRepository<Country, Long> {

    fun findByIso2(iso2: String): Country?

    fun findByIso3(iso3: String): Country?

    @Query("SELECT * FROM countries WHERE is_active = true AND region_id = :regionId")
    fun findByRegionId(regionId: Long, pageable: Pageable): Page<Country>

    @Query("SELECT * FROM countries WHERE is_active = true")
    fun findAllActive(pageable: Pageable): Page<Country>

    @Query("""
        SELECT * FROM countries
        WHERE is_active = true
        AND (name ILIKE CONCAT('%', :search, '%')
             OR iso2 ILIKE CONCAT('%', :search, '%')
             OR iso3 ILIKE CONCAT('%', :search, '%'))
    """)
    fun searchByNameOrCode(search: String, pageable: Pageable): Page<Country>
}
