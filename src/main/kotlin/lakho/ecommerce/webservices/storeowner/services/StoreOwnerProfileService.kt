package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.storeowner.repositories.StoreOwnerProfileRepository
import lakho.ecommerce.webservices.storeowner.repositories.entities.StoreOwnerProfile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StoreOwnerProfileService(
    private val storeOwnerProfileRepository: StoreOwnerProfileRepository
) {

    @Transactional(readOnly = false)
    fun createProfile(userId: UUID): StoreOwnerProfile {
        // Check if profile already exists
        if (storeOwnerProfileRepository.existsByUserId(userId)) {
            throw IllegalStateException("Store owner profile already exists for user: $userId")
        }

        val profile = StoreOwnerProfile(userId = userId)
        return storeOwnerProfileRepository.save(profile)
    }

    @Transactional(readOnly = true)
    fun getProfileByUserId(userId: UUID): StoreOwnerProfile? {
        return storeOwnerProfileRepository.findByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getProfileById(id: UUID): StoreOwnerProfile? {
        return storeOwnerProfileRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = false)
    fun updateProfile(id: UUID, profile: StoreOwnerProfile): StoreOwnerProfile {
        val existing = storeOwnerProfileRepository.findById(id).orElseThrow {
            IllegalArgumentException("Store owner profile not found: $id")
        }

        val updated = existing.copy(
            businessName = profile.businessName ?: existing.businessName,
            businessEmail = profile.businessEmail ?: existing.businessEmail,
            businessPhone = profile.businessPhone ?: existing.businessPhone,
            taxId = profile.taxId ?: existing.taxId,
            businessAddress = profile.businessAddress ?: existing.businessAddress,
            city = profile.city ?: existing.city,
            state = profile.state ?: existing.state,
            country = profile.country ?: existing.country,
            postalCode = profile.postalCode ?: existing.postalCode,
            updatedAt = java.time.Instant.now()
        )

        return storeOwnerProfileRepository.save(updated)
    }
}
