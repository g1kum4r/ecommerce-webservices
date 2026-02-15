package lakho.ecommerce.webservices.storeowner.services

import lakho.ecommerce.webservices.storeowner.api.models.CreateStoreRequest
import lakho.ecommerce.webservices.storeowner.api.models.UpdateStoreRequest
import lakho.ecommerce.webservices.storeowner.repositories.StoreOwnerProfileRepository
import lakho.ecommerce.webservices.storeowner.repositories.StoreRepository
import lakho.ecommerce.webservices.storeowner.repositories.entities.Store
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StoreManagementService(
    private val storeRepository: StoreRepository,
    private val storeOwnerProfileRepository: StoreOwnerProfileRepository
) {

    @Transactional(readOnly = false)
    fun createStore(userId: UUID, request: CreateStoreRequest): Store {
        // Get store owner profile
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        // Generate slug from name
        val slug = generateSlug(request.name)

        // Check if slug already exists
        if (storeRepository.existsBySlug(slug)) {
            throw IllegalArgumentException("Store with slug '$slug' already exists")
        }

        val store = Store(
            storeOwnerProfileId = profile.id!!,
            name = request.name,
            slug = slug,
            description = request.description,
            storeCategoryId = request.storeCategoryId,
            locationType = request.locationType,
            addressId = request.addressId,
            latitude = request.latitude,
            longitude = request.longitude,
            phone = request.phone,
            email = request.email,
            website = request.website,
            businessHours = request.businessHours
        )

        return storeRepository.save(store)
    }

    @Transactional(readOnly = true)
    fun getStoresByOwner(userId: UUID, pageable: Pageable): Page<Store> {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        return storeRepository.findByStoreOwnerProfileId(profile.id!!, pageable)
    }

    @Transactional(readOnly = true)
    fun getStoreById(userId: UUID, storeId: UUID): Store? {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val store = storeRepository.findById(storeId).orElse(null) ?: return null

        // Verify ownership
        if (store.storeOwnerProfileId != profile.id) {
            throw IllegalAccessException("Store does not belong to user")
        }

        return store
    }

    @Transactional(readOnly = false)
    fun updateStore(userId: UUID, storeId: UUID, request: UpdateStoreRequest): Store {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val existing = storeRepository.findById(storeId).orElseThrow {
            IllegalArgumentException("Store not found: $storeId")
        }

        // Verify ownership
        if (existing.storeOwnerProfileId != profile.id) {
            throw IllegalAccessException("Store does not belong to user")
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            description = request.description ?: existing.description,
            storeCategoryId = request.storeCategoryId ?: existing.storeCategoryId,
            locationType = request.locationType ?: existing.locationType,
            addressId = request.addressId ?: existing.addressId,
            latitude = request.latitude ?: existing.latitude,
            longitude = request.longitude ?: existing.longitude,
            phone = request.phone ?: existing.phone,
            email = request.email ?: existing.email,
            website = request.website ?: existing.website,
            businessHours = request.businessHours ?: existing.businessHours,
            updatedAt = java.time.Instant.now()
        )

        return storeRepository.save(updated)
    }

    @Transactional(readOnly = false)
    fun deleteStore(userId: UUID, storeId: UUID) {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val store = storeRepository.findById(storeId).orElseThrow {
            IllegalArgumentException("Store not found: $storeId")
        }

        // Verify ownership
        if (store.storeOwnerProfileId != profile.id) {
            throw IllegalAccessException("Store does not belong to user")
        }

        storeRepository.delete(store)
    }

    @Transactional(readOnly = false)
    fun toggleStoreActive(userId: UUID, storeId: UUID, isActive: Boolean): Store {
        val profile = storeOwnerProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("Store owner profile not found for user: $userId")

        val store = storeRepository.findById(storeId).orElseThrow {
            IllegalArgumentException("Store not found: $storeId")
        }

        // Verify ownership
        if (store.storeOwnerProfileId != profile.id) {
            throw IllegalAccessException("Store does not belong to user")
        }

        val updated = store.copy(isActive = isActive, updatedAt = java.time.Instant.now())
        return storeRepository.save(updated)
    }

    private fun generateSlug(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .take(255)
    }
}
