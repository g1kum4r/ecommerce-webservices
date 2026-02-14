package lakho.ecommerce.webservices.user.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("roles")
data class Role(
    @Id val id: Long? = null,
    val name: String
)
