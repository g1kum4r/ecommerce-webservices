package lakho.ecommerce.webservices.storeowner.api.models

import java.util.UUID

data class StoreProfile(
    val id: UUID,
    val email: String
)
