package lakho.ecommerce.webservices.common.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("store_categories")
data class StoreCategory(
    @Id val id: Long? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val parentId: Long? = null,
    val level: Int = 0,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
