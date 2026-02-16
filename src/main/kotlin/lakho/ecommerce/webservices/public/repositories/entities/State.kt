package lakho.ecommerce.webservices.public.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("states")
data class State(
    @Id val id: Long? = null,
    val countryId: Long,
    val name: String,
    val stateCode: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
