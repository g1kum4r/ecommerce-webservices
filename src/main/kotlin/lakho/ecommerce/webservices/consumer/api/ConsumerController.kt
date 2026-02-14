package lakho.ecommerce.webservices.consumer.api

import lakho.ecommerce.webservices.consumer.api.models.ConsumerProfile
import lakho.ecommerce.webservices.consumer.services.ConsumerService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/consumer")
class ConsumerController(private val consumerService: ConsumerService) {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<ConsumerProfile> {
        val profile = consumerService.getProfileByEmail(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }
}
