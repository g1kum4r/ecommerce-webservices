package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.common.repositories.UserRepository
import lakho.ecommerce.webservices.common.services.UserRoleCacheService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Custom UserDetailsService that loads user details with roles from Redis cache.
 *
 * Uses Cache-Aside pattern via UserRoleCacheService:
 * 1. First checks Redis cache for user roles
 * 2. On cache miss, loads from database and populates cache
 * 3. Returns UserDetails with authorities built from cached roles
 *
 * This significantly reduces database load during authentication.
 */
@Service
@Slf4j
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val userRoleCacheService: UserRoleCacheService
) : UserDetailsService {
    private val logger: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): UserDetails {
        logger.info("Authenticating user: $username")
        val user = userRepository.findByEmailOrUsername(username, username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // Load roles from cache (Cache-Aside pattern)
        val roles = userRoleCacheService.getUserRoles(user.id!!)
        val authorities = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

        logger.debug("Loaded {} roles for user: {}", roles.size, username)

        return User.builder()
            .username(user.email)
            .password(user.passwordHash ?: "{noop}")
            .authorities(authorities)
            .accountExpired(user.accountExpired)
            .accountLocked(user.accountLocked)
            .credentialsExpired(user.credentialsExpired)
            .disabled(!user.enabled)
            .build()
    }
}
