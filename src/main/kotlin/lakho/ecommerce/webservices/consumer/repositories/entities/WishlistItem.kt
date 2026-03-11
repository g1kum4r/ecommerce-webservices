package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("wishlists")
data class WishlistItem(
    @Id val id: UUID? = null,
    val userId: UUID,
    val productId: UUID,
    val createdAt: Instant = Instant.now()
)
