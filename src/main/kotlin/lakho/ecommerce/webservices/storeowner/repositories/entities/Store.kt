package lakho.ecommerce.webservices.storeowner.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class LocationType {
    ONLINE, PHYSICAL, BOTH
}

@Table("stores")
data class Store(
    @Id val id: UUID? = null,
    val storeOwnerProfileId: UUID,
    val name: String,
    val slug: String,
    val description: String? = null,
    val storeCategoryId: Long? = null,
    val locationType: LocationType,
    // Address reference
    val addressId: UUID? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    // Contact
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val businessHours: String? = null,
    // Status
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
