package lakho.ecommerce.webservices.storeowner.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("store_owner_profiles")
data class StoreOwnerProfile(
    @Id val id: UUID? = null,
    val userId: UUID,
    val businessName: String? = null,
    val businessEmail: String? = null,
    val businessPhone: String? = null,
    val taxId: String? = null,
    val businessAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
