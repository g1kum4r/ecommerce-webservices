package lakho.ecommerce.webservices.address.repositories

import lakho.ecommerce.webservices.address.repositories.entities.Region
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface RegionRepository : CrudRepository<Region, Long>, PagingAndSortingRepository<Region, Long> {

    fun findByCode(code: String): Region?

    @Query("SELECT * FROM regions WHERE is_active = true")
    fun findAllActive(pageable: Pageable): Page<Region>

    @Query("SELECT * FROM regions WHERE is_active = true AND (name ILIKE CONCAT('%', :search, '%') OR code ILIKE CONCAT('%', :search, '%'))")
    fun searchByNameOrCode(search: String, pageable: Pageable): Page<Region>
}
