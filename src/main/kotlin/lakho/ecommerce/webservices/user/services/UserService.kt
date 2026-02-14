package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.repositories.UserRepository
import lakho.ecommerce.webservices.user.repositories.UserRoleRepository
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository
) {

    @Transactional(readOnly = true)
    fun findById(id: UUID): User? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        val roles = userRoleRepository.findRolesByUserId(user.id)
        return user.copy(roles = roles)
    }

    @Transactional(readOnly = true)
    fun findByEmailOrUsername(emailOrUsername: String): User? {
        val user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername) ?: return null
        val roles = userRoleRepository.findRolesByUserId(user.id)
        return user.copy(roles = roles)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<User> {
        return userRepository.findAll().map { user ->
            val roles = userRoleRepository.findRolesByUserId(user.id)
            user.copy(roles = roles)
        }
    }

    @Transactional(readOnly = true)
    fun findAllPaginated(pageable: Pageable): Page<User> {
        val allUsers = findAll()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allUsers.size)
        val end = (start + pageable.pageSize).coerceAtMost(allUsers.size)
        val pageContent = allUsers.subList(start, end)
        return PageImpl(pageContent, pageable, allUsers.size.toLong())
    }

    @Transactional(readOnly = true)
    fun findByRoles(roles: Set<UserRole>, pageable: Pageable): Page<User> {
        val usersWithRoles = findAll().filter { user ->
            user.roles.any { it in roles }
        }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(usersWithRoles.size)
        val end = (start + pageable.pageSize).coerceAtMost(usersWithRoles.size)
        val pageContent = usersWithRoles.subList(start, end)
        return PageImpl(pageContent, pageable, usersWithRoles.size.toLong())
    }

    @Transactional
    fun save(user: User, roles: Set<UserRole>): User {
        val savedUser = userRepository.save(user)
        if (savedUser.id != null) {
            userRoleRepository.saveUserRoles(savedUser.id, roles)
        }
        return savedUser.copy(roles = roles)
    }

    @Transactional
    fun update(user: User): User {
        val savedUser = userRepository.save(user)
        if (user.roles.isNotEmpty() && savedUser.id != null) {
            userRoleRepository.saveUserRoles(savedUser.id, user.roles)
        }
        return savedUser
    }

    @Transactional
    fun deleteById(id: UUID) {
        userRoleRepository.deleteUserRoles(id)
        userRepository.deleteById(id)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
}
