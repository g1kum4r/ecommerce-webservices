package lakho.ecommerce.webservices.common.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.util.UUID

data class UserRoleId(val userId: UUID, val roleId: Long) : Serializable

@Table("user_roles")
data class UserRole(
    @Id @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val id: UserRoleId = UserRoleId(UUID.randomUUID(), 0)
) {
    constructor(userId: UUID, roleId: Long) : this(UserRoleId(userId, roleId))

}
