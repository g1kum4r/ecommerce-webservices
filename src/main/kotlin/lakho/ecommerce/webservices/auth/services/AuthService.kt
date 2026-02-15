package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.auth.events.LoginSuccessEvent
import lakho.ecommerce.webservices.event.events.PasswordResetEvent
import lakho.ecommerce.webservices.event.events.UserRegisteredEvent
import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.models.User
import lakho.ecommerce.webservices.user.repositories.models.toUserModel
import lakho.ecommerce.webservices.user.services.UserDataCacheService
import lakho.ecommerce.webservices.user.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.AuthenticationManager
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
    private val authenticationManager: AuthenticationManager,
    private val emailService: EmailService,
    private val tokenService: TokenService,
    private val eventPublisher: ApplicationEventPublisher,
    private val jwtTokenCacheService: JwtTokenCacheService,
    private val userDataCacheService: UserDataCacheService
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

        // Create a verification token and publish event
        try {
            val verificationToken = tokenService.createEmailVerificationToken(user.id)

            // Publish UserRegisteredEvent - listener will handle email sending
            eventPublisher.publishEvent(
                UserRegisteredEvent(
                    source = this,
                    userId = user.id,
                    email = user.email,
                    username = user.username,
                    verificationToken = verificationToken.token
                )
            )
            logger.info("UserRegisteredEvent published: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("Failed to publish UserRegisteredEvent: userId={}, email={}", user.id, user.email, e)
            // Don't fail registration if event publishing fails
        }

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

    fun verifyEmail(token: String) {
        val verificationToken = tokenService.validateEmailVerificationToken(token)
        tokenService.markEmailAsVerified(token)
        
        logger.info("Email verified successfully: userId={}", verificationToken.userId)
    }

    fun forgotPassword(email: String) {
        val user = userService.findByEmailOrUsername(email)
            ?: throw UsernameNotFoundException("User not found: $email")

        try {
            val resetToken = tokenService.createPasswordResetToken(user.id)
            emailService.sendPasswordResetEmail(user.email, user.username, resetToken.token)
            logger.info("Password reset email sent: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("Failed to send password reset email: userId={}, email={}", user.id, user.email, e)
            throw IllegalStateException("Failed to send password reset email")
        }
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = tokenService.validatePasswordResetToken(token)
        
        val user = userService.findById(resetToken.userId)
            ?: throw UsernameNotFoundException("User not found")

        val encodedPassword = passwordEncoder.encode(newPassword)
            ?: throw IllegalStateException("Password encoding failed")

        userService.updatePassword(user.id, encodedPassword)
        tokenService.markPasswordResetTokenAsUsed(token)

        // Publish PasswordResetEvent - listener will handle confirmation email
        try {
            eventPublisher.publishEvent(
                PasswordResetEvent(
                    source = this,
                    userId = user.id,
                    email = user.email,
                    username = user.username
                )
            )
            logger.info("PasswordResetEvent published: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("Failed to publish PasswordResetEvent: userId={}", user.id, e)
            // Don't fail the reset if event publishing fails
        }

        logger.info("Password reset successfully: userId={}, email={}", user.id, user.email)
    }

    fun logout(accessToken: String?) {
        if (accessToken == null) {
            logger.warn("Logout attempt without token")
            return
        }

        try {
            // Extract user email from token to invalidate user data cache
            val email = jwtService.extractEmail(accessToken)

            // Remove token from cache
            jwtTokenCacheService.removeAccessToken(accessToken)

            // Invalidate user data cache
            if (email != null) {
                userDataCacheService.invalidateUserDataCacheByEmail(email)
                logger.info("User logged out successfully: email={}", email)
            } else {
                logger.info("User logged out successfully (email not found in token)")
            }
        } catch (e: Exception) {
            logger.error("Failed to remove token/user data from cache during logout", e)
            throw IllegalStateException("Logout failed")
        }
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val roles = user.roles.joinToString(",") { it.name }
        val accessToken = jwtService.generateAccessToken(user.email, roles)
        val refreshToken = jwtService.generateRefreshToken(user.email, roles)

        // Publish LoginSuccessEvent to cache tokens and user data in Redis
        try {
            eventPublisher.publishEvent(
                LoginSuccessEvent(
                    source = this,
                    user = user,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
            logger.debug("LoginSuccessEvent published: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("Failed to publish LoginSuccessEvent: userId={}, email={}", user.id, user.email, e)
            // Don't fail auth if event publishing fails
        }

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
