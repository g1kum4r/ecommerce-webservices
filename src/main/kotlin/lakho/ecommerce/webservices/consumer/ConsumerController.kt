package lakho.ecommerce.webservices.consumer

import lakho.ecommerce.webservices.consumer.dto.ConsumerProfile
import lakho.ecommerce.webservices.user.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/consumer")
class ConsumerController(private val userRepository: UserRepository) {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<ConsumerProfile> {
        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ConsumerProfile(id = user.id!!, email = user.email))
    }
}
