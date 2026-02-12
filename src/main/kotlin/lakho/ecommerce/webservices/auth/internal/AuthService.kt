package lakho.ecommerce.webservices.auth.internal

import lakho.ecommerce.webservices.auth.dto.AuthResponse
import lakho.ecommerce.webservices.auth.dto.LoginRequest
import lakho.ecommerce.webservices.auth.dto.RefreshRequest
import lakho.ecommerce.webservices.auth.dto.RegisterRequest
import lakho.ecommerce.webservices.user.User
import lakho.ecommerce.webservices.user.UserRepository
import lakho.ecommerce.webservices.user.UserRole
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
internal class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(request: RegisterRequest): AuthResponse {
        require(request.role != UserRole.ADMIN) { "Cannot register as ADMIN" }
        require(!userRepository.existsByEmail(request.email)) { "Email already registered" }

        val user = userRepository.save(
            User(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password)!!,
                role = request.role
            )
        )

        return generateAuthResponse(user)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        require(passwordEncoder.matches(request.password, user.passwordHash)) { "Invalid credentials" }
        require(user.active) { "Account is deactivated" }

        return generateAuthResponse(user)
    }

    fun refresh(request: RefreshRequest): AuthResponse {
        require(jwtService.isTokenValid(request.refreshToken)) { "Invalid refresh token" }

        val email = jwtService.extractEmail(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found")

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val role = user.role.name
        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user.email, role),
            refreshToken = jwtService.generateRefreshToken(user.email, role)
        )
    }
}
