package lakho.ecommerce.webservices.storeowner.repositories

import lakho.ecommerce.webservices.storeowner.repositories.entities.Store
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StoreRepository : CrudRepository<Store, UUID>, PagingAndSortingRepository<Store, UUID> {
    fun findByStoreOwnerProfileId(storeOwnerProfileId: UUID, pageable: Pageable): Page<Store>
    fun findBySlug(slug: String): Store?
    fun existsBySlug(slug: String): Boolean

    @Query("SELECT * FROM stores WHERE store_owner_profile_id = :storeOwnerProfileId")
    fun findAllByStoreOwnerProfileId(storeOwnerProfileId: UUID): List<Store>
}
