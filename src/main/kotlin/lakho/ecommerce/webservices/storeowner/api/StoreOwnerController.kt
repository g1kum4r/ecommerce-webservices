package lakho.ecommerce.webservices.storeowner.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.storeowner.api.models.CreateStoreRequest
import lakho.ecommerce.webservices.storeowner.api.models.StoreProfile
import lakho.ecommerce.webservices.storeowner.api.models.UpdateStoreRequest
import lakho.ecommerce.webservices.storeowner.repositories.entities.Store
import lakho.ecommerce.webservices.storeowner.services.StoreManagementService
import lakho.ecommerce.webservices.storeowner.services.StoreService
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/storeowner")
class StoreOwnerController(
    private val storeService: StoreService,
    private val storeManagementService: StoreManagementService,
    private val userService: UserService
) {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<StoreProfile> {
        val profile = storeService.getProfileByEmail(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }

    // Store Management
    @PostMapping("/stores")
    fun createStore(
        authentication: Authentication,
        @Valid @RequestBody request: CreateStoreRequest
    ): ResponseEntity<Store> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val store = storeManagementService.createStore(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(store)
    }

    @GetMapping("/stores")
    fun getMyStores(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<Store>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val pageable = PageRequest.of(page, size)
        val stores = storeManagementService.getStoresByOwner(user.id, pageable)
        return ResponseEntity.ok(stores)
    }

    @GetMapping("/stores/{id}")
    fun getStoreById(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Store> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val store = storeManagementService.getStoreById(user.id, id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(store)
    }

    @PutMapping("/stores/{id}")
    fun updateStore(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestBody request: UpdateStoreRequest
    ): ResponseEntity<Store> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val updated = storeManagementService.updateStore(user.id, id, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/stores/{id}")
    fun deleteStore(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        storeManagementService.deleteStore(user.id, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/stores/{id}/activate")
    fun toggleStoreActive(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestParam isActive: Boolean
    ): ResponseEntity<Store> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val updated = storeManagementService.toggleStoreActive(user.id, id, isActive)
        return ResponseEntity.ok(updated)
    }
}
