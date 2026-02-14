package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import lakho.ecommerce.webservices.user.services.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
internal class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(request: RegisterRequest): AuthResponse {
        require(request.roles.none { it == UserRole.ADMIN }) { "Cannot register as ADMIN" }
        require(!userService.existsByEmail(request.email)) { "Email already registered" }

        val user = userService.save(
            User(
                email = request.email,
                username = request.email,
                passwordHash = passwordEncoder.encode(request.password)!!
            ),
            request.roles
        )

        return generateAuthResponse(user)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userService.findByEmailOrUsername(request.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        require(passwordEncoder.matches(request.password, user.passwordHash)) { "Invalid credentials" }
        require(user.enabled) { "Account is disabled" }
        require(user.accountLocked) { "Account is locked" }

        return generateAuthResponse(user)
    }

    fun refresh(request: RefreshRequest): AuthResponse {
        require(jwtService.isTokenValid(request.refreshToken)) { "Invalid refresh token" }

        val email = jwtService.extractEmail(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        val user = userService.findByEmailOrUsername(email)
            ?: throw IllegalArgumentException("User not found")

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
