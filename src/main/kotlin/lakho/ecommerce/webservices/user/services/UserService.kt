package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.RoleRepository
import lakho.ecommerce.webservices.user.repositories.UserRepository
import lakho.ecommerce.webservices.user.repositories.UserRoleRepository
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import lakho.ecommerce.webservices.user.repositories.models.SecureUser
import lakho.ecommerce.webservices.user.repositories.models.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository
) {

    fun findById(id: UUID): User? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        val roles = roleRepository.findByUserId(user.id!!)
        return User(user, roles)
    }

    fun findByEmailOrUsername(emailOrUsername: String): User? {
        val user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername) ?: return null
        val roles = roleRepository.findByUserId(user.id!!)
        return User(user, roles)
    }

    fun findSecureUserByEmailOrUsername(emailOrUsername: String): SecureUser? {
        val user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername) ?: return null
        val roles = roleRepository.findByUserId(user.id!!)
        return SecureUser(user, roles)
    }

    fun findAll(): List<User> {
        return userRepository.findAll().map { user ->
            val roles = roleRepository.findByUserId(user.id!!).toSet()
            User(user, roles)
        }
    }

    fun findAllPaginated(pageable: Pageable): Page<User> {
        val allUsers = findAll()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allUsers.size)
        val end = (start + pageable.pageSize).coerceAtMost(allUsers.size)
        val pageContent = allUsers.subList(start, end)
        return PageImpl(pageContent, pageable, allUsers.size.toLong())
    }

    fun findByRoles(roles: Set<Roles>, pageable: Pageable): Page<User> = throw NotImplementedError()

    @Transactional(readOnly = false)
    fun save(
        user: lakho.ecommerce.webservices.user.repositories.entities.User, roles: Set<Roles>
    ): User {
        val savedUser = userRepository.save(user)
        roleRepository.findByNameIn(roles.map { it.name })
            .map { UserRole(savedUser.id!!, it.id) }
            .let { userRoleRepository.saveAll(it) }

        val userRoles = roleRepository.findByUserId(savedUser.id!!)
        return User(savedUser, userRoles)
    }

    @Transactional(readOnly = false)
    fun update(user: lakho.ecommerce.webservices.user.repositories.entities.User): User =
        User(userRepository.save(user))

    @Transactional(readOnly = false)
    fun deleteById(id: UUID) {
        userRoleRepository.deleteByUserId(id)
        userRepository.deleteById(id)
    }

    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)
}
