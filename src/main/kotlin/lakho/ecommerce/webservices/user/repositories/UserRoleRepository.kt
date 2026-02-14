package lakho.ecommerce.webservices.user.repositories

import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import lakho.ecommerce.webservices.user.repositories.entities.UserRoleId
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRoleRepository : ListCrudRepository<UserRole, UserRoleId> {

    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    fun deleteByUserId(userId: UUID)

    @Query("SELECT EXISTS (SELECT 1 FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId)")
    fun existsByUserIdAndRoleId(userId: UUID, roleId: Int): Boolean
}
