package lakho.ecommerce.webservices.admin

import lakho.ecommerce.webservices.admin.dto.UserSummary
import lakho.ecommerce.webservices.user.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(private val userRepository: UserRepository) {

    @GetMapping("/users")
    fun listUsers(): ResponseEntity<List<UserSummary>> {
        val users = userRepository.findAll().map {
            UserSummary(
                id = it.id!!,
                email = it.email,
                role = it.role,
                active = it.active
            )
        }
        return ResponseEntity.ok(users)
    }
}
