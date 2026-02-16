package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.consumer.api.models.ConsumerProfile
import lakho.ecommerce.webservices.common.services.UserService
import org.springframework.stereotype.Service

@Service
class ConsumerService(private val userService: UserService) {

    fun getProfileByEmail(email: String): ConsumerProfile? {
        val user = userService.findByEmailOrUsername(email) ?: return null
        return ConsumerProfile(id = user.id, email = user.email)
    }
}
