package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import lakho.ecommerce.webservices.user.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
internal class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun register(request: RegisterRequest): AuthResponse {
        require(request.roles.none { it == UserRole.ADMIN }) { "Cannot register as ADMIN" }
        require(!userService.existsByEmail(request.email)) { "Email already registered" }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("Password encoding failed")

        val user = userService.save(
            User(
                email = request.email,
                username = request.username,
                passwordHash = encodedPassword
            ),
            request.roles
        )

        logger.info("User registered successfully: userId={}, email={}", user.id, user.email)

        return generateAuthResponse(user)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userService.findByEmailOrUsername(request.email)

        if (user == null) {
            logger.warn("Login attempt with non-existent email: {}", request.email)
            throw BadCredentialsException("Invalid credentials")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            logger.warn("Failed login attempt for user: userId={}, email={}", user.id, user.email)
            throw BadCredentialsException("Invalid credentials")
        }

        if (!user.enabled) {
            logger.warn("Login attempt for disabled account: userId={}", user.id)
            throw IllegalArgumentException("Account is disabled")
        }

        if (user.accountLocked) {
            logger.warn("Login attempt for locked account: userId={}", user.id)
            throw IllegalArgumentException("Account is locked")
        }

        logger.info("User logged in successfully: userId={}, email={}", user.id, user.email)

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

        val user = userService.findByEmailOrUsername(email)

        if (user == null) {
            logger.warn("Refresh token for non-existent user: email={}", email)
            throw IllegalArgumentException("User not found")
        }

        logger.info("Token refreshed successfully: userId={}, email={}", user.id, user.email)

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val roles = user.roles.joinToString(",") { it.name }
        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user.email, roles),
            refreshToken = jwtService.generateRefreshToken(user.email, roles)
        )
    }
}
