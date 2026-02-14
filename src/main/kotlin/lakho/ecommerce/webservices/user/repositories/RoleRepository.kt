package lakho.ecommerce.webservices.user.repositories

import lakho.ecommerce.webservices.user.repositories.entities.Role
import org.springframework.data.repository.ListCrudRepository

interface RoleRepository : ListCrudRepository<Role, Long> {
    fun findByName(name: String): Role?
}
