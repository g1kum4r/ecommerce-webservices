package lakho.ecommerce.webservices.common.repositories

import lakho.ecommerce.webservices.common.repositories.entities.User
import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface UserRepository : ListCrudRepository<User, UUID> {
    fun findByEmailOrUsername(email: String, username: String): User?
    fun existsByEmail(email: String): Boolean
}
