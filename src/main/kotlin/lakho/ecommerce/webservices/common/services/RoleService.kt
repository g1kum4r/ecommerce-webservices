package lakho.ecommerce.webservices.common.services

import lakho.ecommerce.webservices.common.repositories.RoleRepository
import lakho.ecommerce.webservices.common.repositories.entities.Role
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    fun findByRoleName(roleName: String): Role? = roleRepository.findByName(roleName)
}