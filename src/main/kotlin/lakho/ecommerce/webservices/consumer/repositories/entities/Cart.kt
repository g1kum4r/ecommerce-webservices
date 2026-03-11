package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("carts")
data class Cart(
    @Id val id: UUID? = null,
    val userId: UUID? = null,
    val sessionId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
