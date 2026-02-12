package lakho.ecommerce.webservices.store

import lakho.ecommerce.webservices.store.dto.StoreProfile
import lakho.ecommerce.webservices.user.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/store")
class StoreController(private val userRepository: UserRepository) {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<StoreProfile> {
        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(StoreProfile(id = user.id!!, email = user.email))
    }
}
