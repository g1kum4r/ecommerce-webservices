package lakho.ecommerce.webservices.consumer.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.consumer.api.models.CreateAddressRequest
import lakho.ecommerce.webservices.consumer.api.models.UpdateAddressRequest
import lakho.ecommerce.webservices.consumer.repositories.entities.ConsumerAddress
import lakho.ecommerce.webservices.consumer.services.ConsumerAddressService
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/consumer/addresses")
class ConsumerAddressController(
    private val consumerAddressService: ConsumerAddressService,
    private val userService: UserService
) {
    @GetMapping
    fun getAddresses(authentication: Authentication): ResponseEntity<List<ConsumerAddress>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(consumerAddressService.getAddresses(user.id))
    }

    @PostMapping
    fun createAddress(
        authentication: Authentication,
        @Valid @RequestBody request: CreateAddressRequest
    ): ResponseEntity<ConsumerAddress> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val address = consumerAddressService.createAddress(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(address)
    }

    @PutMapping("/{id}")
    fun updateAddress(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestBody request: UpdateAddressRequest
    ): ResponseEntity<ConsumerAddress> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val address = consumerAddressService.updateAddress(user.id, id, request)
        return ResponseEntity.ok(address)
    }

    @DeleteMapping("/{id}")
    fun deleteAddress(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        consumerAddressService.deleteAddress(user.id, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/default")
    fun setDefault(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<ConsumerAddress> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val address = consumerAddressService.setDefault(user.id, id)
        return ResponseEntity.ok(address)
    }
}
