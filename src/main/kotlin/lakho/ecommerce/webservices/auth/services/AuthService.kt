package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.models.User
import lakho.ecommerce.webservices.user.repositories.models.toUserModel
import lakho.ecommerce.webservices.user.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)


    @Transactional(readOnly = false, transactionManager = "transactionManager")
    fun register(request: RegisterRequest): AuthResponse {
        require(request.roles.none { it == Roles.ADMIN }) { "Cannot register as ADMIN" }
        require(!userService.existsByEmail(request.email)) { "Email already registered" }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("Password encoding failed")

        val user = userService.save(
            lakho.ecommerce.webservices.user.repositories.entities.User(
                email = request.email,
                username = request.email,
                passwordHash = encodedPassword
            ),
            request.roles
        )

        logger.info("User registered successfully: userId={}, email={}", user.id, user.email)

        return generateAuthResponse(user)
    }

    fun login(request: LoginRequest): AuthResponse {

        val user = userService.findByEmailOrUsername(request.email)
            ?: throw UsernameNotFoundException("User not found: ${request.email}")

        this.authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )

        return generateAuthResponse(user)
    }

    fun refresh(request: RefreshRequest): AuthResponse {
        if (!jwtService.isTokenValid(request.refreshToken)) {
            logger.warn("Invalid refresh token attempt")
            throw IllegalArgumentException("Invalid refresh token")
        }

        val email = jwtService.extractEmail(request.refreshToken)

        if (email == null) {
            logger.warn("Refresh token with no email claim")
            throw IllegalArgumentException("Invalid refresh token")
        }

        val secureUser = userService.findSecureUserByEmailOrUsername(email)

        if (secureUser == null) {
            logger.warn("Refresh token for non-existent user: email={}", email)
            throw IllegalArgumentException("User not found")
        }

        logger.info("Token refreshed successfully: userId={}, email={}", secureUser.id, secureUser.email)

        return generateAuthResponse(secureUser.toUserModel())
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val roles = user.roles.joinToString(",") { it.name }
        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user.email, roles),
            refreshToken = jwtService.generateRefreshToken(user.email, roles)
        )
    }
}
