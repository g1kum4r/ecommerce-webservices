package lakho.ecommerce.webservices.common.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("roles")
data class Role(
    @Id val id: Long,
    val name: String
)
