package lakho.ecommerce.webservices.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class User(
    @Id val id: Long? = null,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val active: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
