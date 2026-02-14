package lakho.ecommerce.webservices.store.services

import lakho.ecommerce.webservices.store.api.models.StoreProfile
import lakho.ecommerce.webservices.user.services.UserService
import org.springframework.stereotype.Service

@Service
class StoreService(private val userService: UserService) {

    fun getProfileByEmail(email: String): StoreProfile? {
        val user = userService.findByEmailOrUsername(email) ?: return null
        return StoreProfile(id = user.id, email = user.email)
    }
}
