package lakho.ecommerce.webservices.common.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("cities")
data class City(
    @Id val id: Long? = null,
    val stateId: Long,
    val name: String,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
