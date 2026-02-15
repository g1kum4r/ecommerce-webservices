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
    private val authenticationManager: AuthenticationManager,
    private val emailService: EmailService,
    private val tokenService: TokenService
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

        // Create a verification token and send an email
        try {
            val verificationToken = tokenService.createEmailVerificationToken(user.id)
            emailService.sendVerificationEmail(user.email, user.username, verificationToken.token)
            logger.info("Verification email sent: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("Failed to send verification email: userId={}, email={}", user.id, user.email, e)
            // Don't fail registration if email fails
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

        try {
            emailService.sendPasswordResetConfirmationEmail(user.email, user.username)
        } catch (e: Exception) {
            logger.error("Failed to send password reset confirmation email: userId={}", user.id, e)
            // Don't fail the reset if confirmation email fails
        }

        logger.info("Password reset successfully: userId={}, email={}", user.id, user.email)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val roles = user.roles.joinToString(",") { it.name }
        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user.email, roles),
            refreshToken = jwtService.generateRefreshToken(user.email, roles)
        )
    }
}
