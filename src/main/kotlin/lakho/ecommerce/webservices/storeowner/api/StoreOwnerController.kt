package lakho.ecommerce.webservices.storeowner.api

import lakho.ecommerce.webservices.storeowner.api.models.StoreProfile
import lakho.ecommerce.webservices.storeowner.services.StoreService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/storeowner")
class StoreOwnerController(private val storeService: StoreService) {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<StoreProfile> {
        val profile = storeService.getProfileByEmail(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }
}
