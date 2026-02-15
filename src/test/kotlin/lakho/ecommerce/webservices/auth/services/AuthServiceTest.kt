package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthServiceTest {

    private lateinit var userService: UserService
    private lateinit var jwtService: JwtService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        jwtService = mock(JwtService::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        authenticationManager = mock(AuthenticationManager::class.java)
        authService = AuthService(
            userService,
            jwtService,
            passwordEncoder,
            authenticationManager,
            emailService = mock(EmailService::class.java),
            tokenService = mock(TokenService::class.java)
        )
    }

    @Test
    fun `register should create new user successfully`() {
        // Arrange
        val request = RegisterRequest(
            email = "test@example.com",
            password = "P@ssw0rd123",
            roles = setOf(Roles.CONSUMER)
        )
        val encodedPassword = "encodedPassword"
        val savedUser = lakho.ecommerce.webservices.user.repositories.models.User(
            id = UUID.randomUUID(),
            email = request.email,
            username = request.email,
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            roles = setOf(lakho.ecommerce.webservices.user.repositories.entities.Role(1, "CONSUMER"))
        )
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"

        `when`(userService.existsByEmail(request.email)).thenReturn(false)
        `when`(passwordEncoder.encode(request.password)).thenReturn(encodedPassword)
        `when`(userService.save(any(User::class.java), eq(request.roles))).thenReturn(savedUser)
        `when`(jwtService.generateAccessToken(savedUser.email, "CONSUMER")).thenReturn(accessToken)
        `when`(jwtService.generateRefreshToken(savedUser.email, "CONSUMER")).thenReturn(refreshToken)

        // Act
        val response = authService.register(request)

        // Assert
        assertNotNull(response)
        assertEquals(accessToken, response.accessToken)
        assertEquals(refreshToken, response.refreshToken)
        verify(userService).existsByEmail(request.email)
        verify(passwordEncoder).encode(request.password)
        verify(userService).save(any(User::class.java), eq(request.roles))
    }

    @Test
    fun `register should throw exception when trying to register as ADMIN`() {
        // Arrange
        val request = RegisterRequest(
            email = "admin@example.com",
            password = "P@ssw0rd123",
            roles = setOf(Roles.ADMIN)
        )

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
        assertEquals("Cannot register as ADMIN", exception.message)
    }

    @Test
    fun `register should throw exception when email already exists`() {
        // Arrange
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "P@ssw0rd123",
            roles = setOf(Roles.CONSUMER)
        )

        `when`(userService.existsByEmail(request.email)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
        assertEquals("Email already registered", exception.message)
    }

    @Test
    fun `login should authenticate user successfully`() {
        // Arrange
        val request = LoginRequest(
            email = "test@example.com",
            password = "P@ssw0rd123"
        )
        val user = lakho.ecommerce.webservices.user.repositories.models.User(
            id = UUID.randomUUID(),
            email = request.email,
            username = request.email,
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            roles = setOf(lakho.ecommerce.webservices.user.repositories.entities.Role(1, "CONSUMER"))
        )
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"

        `when`(userService.findByEmailOrUsername(request.email)).thenReturn(user)
        `when`(authenticationManager.authenticate(any())).thenReturn(null)
        `when`(jwtService.generateAccessToken(user.email, "CONSUMER")).thenReturn(accessToken)
        `when`(jwtService.generateRefreshToken(user.email, "CONSUMER")).thenReturn(refreshToken)

        // Act
        val response = authService.login(request)

        // Assert
        assertNotNull(response)
        assertEquals(accessToken, response.accessToken)
        assertEquals(refreshToken, response.refreshToken)
        verify(userService).findByEmailOrUsername(request.email)
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken::class.java))
    }

    @Test
    fun `login should throw exception when user not found`() {
        // Arrange
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "P@ssw0rd123"
        )

        `when`(userService.findByEmailOrUsername(request.email)).thenReturn(null)

        // Act & Assert
        assertThrows<UsernameNotFoundException> {
            authService.login(request)
        }
    }

    @Test
    fun `login should throw exception when credentials are invalid`() {
        // Arrange
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )
        val user = lakho.ecommerce.webservices.user.repositories.models.User(
            id = UUID.randomUUID(),
            email = request.email,
            username = request.email,
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            roles = setOf(lakho.ecommerce.webservices.user.repositories.entities.Role(1, "CONSUMER"))
        )

        `when`(userService.findByEmailOrUsername(request.email)).thenReturn(user)
        `when`(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("Invalid credentials"))

        // Act & Assert
        assertThrows<BadCredentialsException> {
            authService.login(request)
        }
    }

    @Test
    fun `refresh should generate new tokens successfully`() {
        // Arrange
        val refreshToken = "validRefreshToken"
        val request = RefreshRequest(refreshToken = refreshToken)
        val email = "test@example.com"
        val secureUser = lakho.ecommerce.webservices.user.repositories.models.SecureUser(
            id = UUID.randomUUID(),
            email = email,
            username = email,
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            roles = setOf(lakho.ecommerce.webservices.user.repositories.entities.Role(1, "CONSUMER"))
        )
        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        `when`(jwtService.isTokenValid(refreshToken)).thenReturn(true)
        `when`(jwtService.extractEmail(refreshToken)).thenReturn(email)
        `when`(userService.findSecureUserByEmailOrUsername(email)).thenReturn(secureUser)
        `when`(jwtService.generateAccessToken(email, "CONSUMER")).thenReturn(newAccessToken)
        `when`(jwtService.generateRefreshToken(email, "CONSUMER")).thenReturn(newRefreshToken)

        // Act
        val response = authService.refresh(request)

        // Assert
        assertNotNull(response)
        assertEquals(newAccessToken, response.accessToken)
        assertEquals(newRefreshToken, response.refreshToken)
        verify(jwtService).isTokenValid(refreshToken)
        verify(jwtService).extractEmail(refreshToken)
        verify(userService).findSecureUserByEmailOrUsername(email)
    }

    @Test
    fun `refresh should throw exception when token is invalid`() {
        // Arrange
        val refreshToken = "invalidToken"
        val request = RefreshRequest(refreshToken = refreshToken)

        `when`(jwtService.isTokenValid(refreshToken)).thenReturn(false)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.refresh(request)
        }
        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refresh should throw exception when email is null`() {
        // Arrange
        val refreshToken = "tokenWithoutEmail"
        val request = RefreshRequest(refreshToken = refreshToken)

        `when`(jwtService.isTokenValid(refreshToken)).thenReturn(true)
        `when`(jwtService.extractEmail(refreshToken)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.refresh(request)
        }
        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refresh should throw exception when user not found`() {
        // Arrange
        val refreshToken = "validToken"
        val request = RefreshRequest(refreshToken = refreshToken)
        val email = "nonexistent@example.com"

        `when`(jwtService.isTokenValid(refreshToken)).thenReturn(true)
        `when`(jwtService.extractEmail(refreshToken)).thenReturn(email)
        `when`(userService.findSecureUserByEmailOrUsername(email)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.refresh(request)
        }
        assertEquals("User not found", exception.message)
    }
}
