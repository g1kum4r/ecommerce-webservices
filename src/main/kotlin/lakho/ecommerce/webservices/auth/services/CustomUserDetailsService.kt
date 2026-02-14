package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.user.repositories.RoleRepository
import lakho.ecommerce.webservices.user.repositories.UserRepository
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@Slf4j
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) : UserDetailsService {
    private val logger: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): UserDetails {
        logger.info("Authenticating user: $username")
        val user = userRepository.findByEmailOrUsername(username, username)
            ?: throw UsernameNotFoundException("User not found: $username")

        val roles = roleRepository.findByUserId(user.id!!)
        val authorities = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

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
