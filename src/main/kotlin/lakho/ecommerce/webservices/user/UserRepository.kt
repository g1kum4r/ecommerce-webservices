package lakho.ecommerce.webservices.user

import org.springframework.data.repository.ListCrudRepository

interface UserRepository : ListCrudRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
