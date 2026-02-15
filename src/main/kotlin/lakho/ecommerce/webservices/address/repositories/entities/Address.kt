package lakho.ecommerce.webservices.address.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Table("addresses")
data class Address(
    @Id val id: UUID? = null,
    val cityId: Long,
    val addressLine1: String,
    val addressLine2: String? = null,
    val streetNo: String? = null,
    val area: String? = null,
    val division: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
