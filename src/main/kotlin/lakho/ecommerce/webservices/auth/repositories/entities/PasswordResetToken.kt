package lakho.ecommerce.webservices.auth.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("password_reset_tokens")
data class PasswordResetToken(
    @Id val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val token: String,
    val expiresAt: Instant,
    val usedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
    fun isUsed(): Boolean = usedAt != null
}
