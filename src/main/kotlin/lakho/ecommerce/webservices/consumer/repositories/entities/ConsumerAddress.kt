package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("consumer_addresses")
data class ConsumerAddress(
    @Id val id: UUID? = null,
    val userId: UUID,
    val label: String = "Home",
    val recipientName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val cityId: Long? = null,
    val stateId: Long? = null,
    val countryId: Long? = null,
    val postalCode: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
