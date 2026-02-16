package lakho.ecommerce.webservices.public.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("regions")
data class Region(
    @Id val id: Long? = null,
    val name: String,
    val code: String,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
