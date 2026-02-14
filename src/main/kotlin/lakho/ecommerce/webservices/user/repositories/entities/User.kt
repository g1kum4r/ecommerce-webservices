package lakho.ecommerce.webservices.user.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("users")
data class User(
    @Id var id: UUID? = null,
    val email: String,
    val username: String,
    val passwordHash: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val accountExpired: Boolean = false,
    val accountLocked: Boolean = false,
    val credentialsExpired: Boolean = false,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
