package lakho.ecommerce.webservices.admin.services

import lakho.ecommerce.webservices.admin.api.models.UserSummary
import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import lakho.ecommerce.webservices.user.services.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminService(private val userService: UserService) {

    fun getAllUsers(pageable: Pageable): Page<UserSummary> {
        return userService.findAllPaginated(pageable).map {
            UserSummary(
                id = it.id,
                email = it.email,
                username = it.username,
                roles = it.roles,
                accountNonExpired = it.accountExpired,
                accountNonLocked = it.accountLocked,
                credentialsNonExpired = it.credentialsExpired,
                enabled = it.enabled
            )
        }
    }

    fun getConsumers(pageable: Pageable): Page<UserSummary> {
        return userService.findByRoles(setOf(Roles.CONSUMER), pageable).map {
            UserSummary(
                id = it.id,
                username = it.username,
                email = it.email,
                roles = it.roles,
                accountNonExpired = it.accountExpired,
                accountNonLocked = it.accountLocked,
                credentialsNonExpired = it.credentialsExpired,
                enabled = it.enabled
            )
        }
    }

    fun getStores(pageable: Pageable): Page<UserSummary> {
        return userService.findByRoles(setOf(Roles.STORE), pageable).map {
            UserSummary(
                id = it.id,
                username = it.username,
                email = it.email,
                roles = it.roles,
                accountNonExpired = it.accountExpired,
                accountNonLocked = it.accountLocked,
                credentialsNonExpired = it.credentialsExpired,
                enabled = it.enabled
            )
        }
    }

    fun getUserById(id: UUID): UserSummary? {
        val user = userService.findById(id) ?: return null
        return UserSummary(
            id = user.id,
            username = user.username,
            email = user.email,
            roles = user.roles,
            accountNonExpired = user.accountExpired,
            accountNonLocked = user.accountLocked,
            credentialsNonExpired = user.credentialsExpired,
            enabled = user.enabled
        )
    }
}
