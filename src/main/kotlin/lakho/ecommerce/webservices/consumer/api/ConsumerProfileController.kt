package lakho.ecommerce.webservices.consumer.api

import lakho.ecommerce.webservices.consumer.api.models.ConsumerProfileResponse
import lakho.ecommerce.webservices.consumer.api.models.UpdateConsumerProfileRequest
import lakho.ecommerce.webservices.consumer.services.ConsumerProfileService
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/consumer/profile")
class ConsumerProfileController(
    private val consumerProfileService: ConsumerProfileService,
    private val userService: UserService
) {
    @GetMapping
    fun getProfile(authentication: Authentication): ResponseEntity<ConsumerProfileResponse> {
        val profile = consumerProfileService.getProfile(authentication.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }

    @PutMapping
    fun updateProfile(
        authentication: Authentication,
        @RequestBody request: UpdateConsumerProfileRequest
    ): ResponseEntity<ConsumerProfileResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val profile = consumerProfileService.updateProfile(user.id, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }
}
