package lakho.ecommerce.webservices.consumer.repositories.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table("consumer_profiles")
data class ConsumerProfile(
    @Id val id: UUID? = null,
    val userId: UUID,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
