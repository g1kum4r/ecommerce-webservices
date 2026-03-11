package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.consumer.api.models.CreateAddressRequest
import lakho.ecommerce.webservices.consumer.api.models.UpdateAddressRequest
import lakho.ecommerce.webservices.consumer.repositories.ConsumerAddressRepository
import lakho.ecommerce.webservices.consumer.repositories.entities.ConsumerAddress
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ConsumerAddressService(
    private val consumerAddressRepository: ConsumerAddressRepository
) {
    @Transactional(readOnly = true)
    fun getAddresses(userId: UUID): List<ConsumerAddress> {
        return consumerAddressRepository.findByUserId(userId)
    }

    @Transactional(readOnly = false)
    fun createAddress(userId: UUID, request: CreateAddressRequest): ConsumerAddress {
        if (request.isDefault) {
            consumerAddressRepository.clearDefaultForUser(userId)
        }
        val address = ConsumerAddress(
            userId = userId,
            label = request.label,
            recipientName = request.recipientName,
            phone = request.phone,
            addressLine1 = request.addressLine1,
            addressLine2 = request.addressLine2,
            cityId = request.cityId,
            stateId = request.stateId,
            countryId = request.countryId,
            postalCode = request.postalCode,
            isDefault = request.isDefault
        )
        return consumerAddressRepository.save(address)
    }

    @Transactional(readOnly = false)
    fun updateAddress(userId: UUID, addressId: UUID, request: UpdateAddressRequest): ConsumerAddress {
        val existing = consumerAddressRepository.findById(addressId).orElseThrow {
            IllegalArgumentException("Address not found: $addressId")
        }
        if (existing.userId != userId) {
            throw IllegalAccessException("Address does not belong to user")
        }
        val updated = existing.copy(
            label = request.label ?: existing.label,
            recipientName = request.recipientName ?: existing.recipientName,
            phone = request.phone ?: existing.phone,
            addressLine1 = request.addressLine1 ?: existing.addressLine1,
            addressLine2 = request.addressLine2 ?: existing.addressLine2,
            cityId = request.cityId ?: existing.cityId,
            stateId = request.stateId ?: existing.stateId,
            countryId = request.countryId ?: existing.countryId,
            postalCode = request.postalCode ?: existing.postalCode,
            updatedAt = Instant.now()
        )
        return consumerAddressRepository.save(updated)
    }

    @Transactional(readOnly = false)
    fun deleteAddress(userId: UUID, addressId: UUID) {
        val address = consumerAddressRepository.findById(addressId).orElseThrow {
            IllegalArgumentException("Address not found: $addressId")
        }
        if (address.userId != userId) {
            throw IllegalAccessException("Address does not belong to user")
        }
        consumerAddressRepository.delete(address)
    }

    @Transactional(readOnly = false)
    fun setDefault(userId: UUID, addressId: UUID): ConsumerAddress {
        val address = consumerAddressRepository.findById(addressId).orElseThrow {
            IllegalArgumentException("Address not found: $addressId")
        }
        if (address.userId != userId) {
            throw IllegalAccessException("Address does not belong to user")
        }
        consumerAddressRepository.clearDefaultForUser(userId)
        val updated = address.copy(isDefault = true, updatedAt = Instant.now())
        return consumerAddressRepository.save(updated)
    }
}
