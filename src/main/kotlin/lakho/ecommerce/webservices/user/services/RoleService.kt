package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.repositories.RoleRepository
import lakho.ecommerce.webservices.user.repositories.entities.Role
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    fun findByRoleName(roleName: String): Role? = roleRepository.findByName(roleName)
}