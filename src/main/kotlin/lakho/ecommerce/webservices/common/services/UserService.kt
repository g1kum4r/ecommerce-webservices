package lakho.ecommerce.webservices.common.services

import lakho.ecommerce.webservices.common.enums.Roles
import lakho.ecommerce.webservices.auth.events.RoleOperation
import lakho.ecommerce.webservices.auth.events.UserOperation
import lakho.ecommerce.webservices.auth.events.UserRoleUpdatedEvent
import lakho.ecommerce.webservices.auth.events.UserUpdatedEvent
import lakho.ecommerce.webservices.common.repositories.RoleRepository
import lakho.ecommerce.webservices.common.repositories.UserRepository
import lakho.ecommerce.webservices.common.repositories.UserRoleRepository
import lakho.ecommerce.webservices.common.repositories.entities.UserRole
import lakho.ecommerce.webservices.common.repositories.models.SecureUser
import lakho.ecommerce.webservices.common.repositories.models.User
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
    private val jdbcAggregateTemplate: JdbcAggregateTemplate,
    private val userRoleCacheService: UserRoleCacheService,
    private val userDataCacheService: UserDataCacheService,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * Find user by ID.
     * Note: User data cache is available via userDataCacheService for display purposes,
     * but this method loads full user data from database for complete information.
     */
    fun findById(id: UUID): User? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        val roles = userRoleCacheService.getUserRoles(user.id!!)
        return User(user, roles)
    }

    /**
     * Find user by email or username.
     * Note: User data cache is available via userDataCacheService for display purposes,
     * but this method loads full user data from database for complete information.
     */
    fun findByEmailOrUsername(emailOrUsername: String): User? {
        val user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername) ?: return null
        val roles = userRoleCacheService.getUserRoles(user.id!!)
        return User(user, roles)
    }

    fun findSecureUserByEmailOrUsername(emailOrUsername: String): SecureUser? {
        val user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername) ?: return null
        val roles = userRoleCacheService.getUserRoles(user.id!!)
        return SecureUser(user, roles)
    }

    fun findAll(): List<User> {
        return userRepository.findAll().map { user ->
            val roles = userRoleCacheService.getUserRoles(user.id!!)
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
        user: lakho.ecommerce.webservices.common.repositories.entities.User, roles: Set<Roles>
    ): User {
        val savedUser = userRepository.save(user)
        val roles = roleRepository.findByNameIn(roles.map { it.name }).toSet()

        roles.map { UserRole(savedUser.id!!, it.id) }
            .let { jdbcAggregateTemplate.insertAll(it) }

        // Cache the roles immediately after assignment
        userRoleCacheService.cacheUserRoles(savedUser.id!!, roles)

        // Publish event for cache invalidation on transaction commit
        eventPublisher.publishEvent(
            UserRoleUpdatedEvent(
                source = this,
                userId = savedUser.id!!,
                operation = RoleOperation.ASSIGNED
            )
        )

        return User(savedUser, roles)
    }

    @Transactional(readOnly = false)
    fun update(user: lakho.ecommerce.webservices.common.repositories.entities.User): User {
        val updatedUser = userRepository.save(user)

        // Publish event to invalidate user data cache after transaction commits
        eventPublisher.publishEvent(
            UserUpdatedEvent(
                source = this,
                userId = updatedUser.id!!,
                operation = UserOperation.PROFILE_UPDATED
            )
        )

        return User(updatedUser)
    }

    @Transactional(readOnly = false)
    fun updatePassword(userId: UUID, newPasswordHash: String) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found: $userId")
        }
        userRepository.save(user.copy(passwordHash = newPasswordHash))

        // Publish event (PASSWORD_CHANGED doesn't invalidate cache)
        eventPublisher.publishEvent(
            UserUpdatedEvent(
                source = this,
                userId = userId,
                operation = UserOperation.PASSWORD_CHANGED
            )
        )
    }

    @Transactional(readOnly = false)
    fun deleteById(id: UUID) {
        userRoleRepository.deleteByUserId(id)
        userRepository.deleteById(id)

        // Invalidate role cache
        userRoleCacheService.invalidateUserRolesCache(id)

        // Publish event to invalidate user data cache
        eventPublisher.publishEvent(
            UserUpdatedEvent(
                source = this,
                userId = id,
                operation = UserOperation.DELETED
            )
        )
    }

    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)
}
