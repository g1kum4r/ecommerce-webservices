package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.consumer.api.models.ConsumerProfileResponse
import lakho.ecommerce.webservices.consumer.api.models.UpdateConsumerProfileRequest
import lakho.ecommerce.webservices.consumer.repositories.ConsumerProfileRepository
import lakho.ecommerce.webservices.consumer.repositories.entities.ConsumerProfile
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ConsumerProfileService(
    private val consumerProfileRepository: ConsumerProfileRepository,
    private val userService: UserService
) {
    @Transactional(readOnly = false)
    fun createProfile(userId: UUID): ConsumerProfile {
        if (consumerProfileRepository.existsByUserId(userId)) {
            throw IllegalStateException("Consumer profile already exists for user: $userId")
        }
        val profile = ConsumerProfile(userId = userId)
        return consumerProfileRepository.save(profile)
    }

    @Transactional(readOnly = true)
    fun getProfile(email: String): ConsumerProfileResponse? {
        val user = userService.findByEmailOrUsername(email) ?: return null
        val profile = consumerProfileRepository.findByUserId(user.id)
        return ConsumerProfileResponse(
            id = user.id,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            phone = profile?.phone,
            dateOfBirth = profile?.dateOfBirth,
            gender = profile?.gender,
            avatarUrl = profile?.avatarUrl
        )
    }

    @Transactional(readOnly = false)
    fun updateProfile(userId: UUID, request: UpdateConsumerProfileRequest): ConsumerProfileResponse? {
        val user = userService.findById(userId) ?: return null
        var profile = consumerProfileRepository.findByUserId(userId)

        if (profile == null) {
            profile = consumerProfileRepository.save(ConsumerProfile(userId = userId))
        }

        val updated = profile.copy(
            phone = request.phone ?: profile.phone,
            dateOfBirth = request.dateOfBirth ?: profile.dateOfBirth,
            gender = request.gender ?: profile.gender,
            avatarUrl = request.avatarUrl ?: profile.avatarUrl,
            updatedAt = Instant.now()
        )
        consumerProfileRepository.save(updated)

        return ConsumerProfileResponse(
            id = user.id,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            phone = updated.phone,
            dateOfBirth = updated.dateOfBirth,
            gender = updated.gender,
            avatarUrl = updated.avatarUrl
        )
    }
}
