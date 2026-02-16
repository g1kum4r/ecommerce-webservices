package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.Role
import lakho.ecommerce.webservices.common.repositories.entities.mapper.RoleMapper
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface RoleRepository : ListCrudRepository<Role, Long> {
    fun findByName(name: String): Role?
    fun findByNameIn(names: List<String>): List<Role>

    @Query(
        """
        SELECT r.* FROM user_roles ur 
        JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = :userId
    """, rowMapperClass = RoleMapper::class
    )
    fun findByUserId(userId: UUID): Set<Role>
}
