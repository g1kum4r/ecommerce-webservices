package lakho.ecommerce.webservices.store.api.models

import java.util.UUID

data class StoreProfile(
    val id: UUID,
    val email: String
)
