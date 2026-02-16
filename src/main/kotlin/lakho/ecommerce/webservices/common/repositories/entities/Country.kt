package lakho.ecommerce.webservices.common.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("countries")
data class Country(
    @Id val id: Long? = null,
    val regionId: Long,
    val name: String,
    val iso2: String,
    val iso3: String,
    val phoneCode: String? = null,
    val language: String? = null,
    val languageCode: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
