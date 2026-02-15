package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.repositories.EmailVerificationTokenRepository
import lakho.ecommerce.webservices.auth.repositories.PasswordResetTokenRepository
import lakho.ecommerce.webservices.auth.repositories.entities.EmailVerificationToken
import lakho.ecommerce.webservices.auth.repositories.entities.PasswordResetToken
import lakho.ecommerce.webservices.config.EmailProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class TokenServiceTest {

    private lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository
    private lateinit var emailProperties: EmailProperties
    private lateinit var tokenService: TokenService

    @BeforeEach
    fun setup() {
        emailVerificationTokenRepository = mock()
        passwordResetTokenRepository = mock()
        
        emailProperties = EmailProperties(
            mail = EmailProperties.MailSettings(
                from = "noreply@test.com",
                fromName = "Test Platform"
            ),
            baseUrl = "http://localhost:8080",
            frontendUrl = "http://localhost:3000",
            emailVerificationTokenExpirationHours = 24,
            passwordResetTokenExpirationHours = 1
        )
        
        tokenService = TokenService(
            emailVerificationTokenRepository,
            passwordResetTokenRepository,
            emailProperties
        )
    }

    @Test
    fun `createEmailVerificationToken should create and save token`() {
        // Arrange
        val userId = UUID.randomUUID()
        val token = EmailVerificationToken(
            userId = userId,
            token = "test-token",
            expiresAt = Instant.now().plusSeconds(86400)
        )
        
        whenever(emailVerificationTokenRepository.deleteByUserId(userId)).thenReturn(0)
        whenever(emailVerificationTokenRepository.save(any())).thenReturn(token)

        // Act
        val result = tokenService.createEmailVerificationToken(userId)

        // Assert
        assertNotNull(result)
        assertEquals(userId, result.userId)
        verify(emailVerificationTokenRepository).deleteByUserId(userId)
        verify(emailVerificationTokenRepository).save(any())
    }

    @Test
    fun `createPasswordResetToken should create and save token`() {
        // Arrange
        val userId = UUID.randomUUID()
        val token = PasswordResetToken(
            userId = userId,
            token = "test-reset-token",
            expiresAt = Instant.now().plusSeconds(3600)
        )
        
        whenever(passwordResetTokenRepository.deleteByUserId(userId)).thenReturn(0)
        whenever(passwordResetTokenRepository.save(any())).thenReturn(token)

        // Act
        val result = tokenService.createPasswordResetToken(userId)

        // Assert
        assertNotNull(result)
        assertEquals(userId, result.userId)
        verify(passwordResetTokenRepository).deleteByUserId(userId)
        verify(passwordResetTokenRepository).save(any())
    }

    @Test
    fun `validateEmailVerificationToken should throw exception for invalid token`() {
        // Arrange
        whenever(emailVerificationTokenRepository.findByToken(any())).thenReturn(null)

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            tokenService.validateEmailVerificationToken("invalid-token")
        }
    }

    @Test
    fun `validateEmailVerificationToken should throw exception for expired token`() {
        // Arrange
        val expiredToken = EmailVerificationToken(
            userId = UUID.randomUUID(),
            token = "expired-token",
            expiresAt = Instant.now().minusSeconds(3600)
        )
        whenever(emailVerificationTokenRepository.findByToken("expired-token")).thenReturn(expiredToken)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            tokenService.validateEmailVerificationToken("expired-token")
        }
        assertEquals("Verification token has expired", exception.message)
    }

    @Test
    fun `validateEmailVerificationToken should throw exception for already verified token`() {
        // Arrange
        val verifiedToken = EmailVerificationToken(
            userId = UUID.randomUUID(),
            token = "verified-token",
            expiresAt = Instant.now().plusSeconds(3600),
            verifiedAt = Instant.now()
        )
        whenever(emailVerificationTokenRepository.findByToken("verified-token")).thenReturn(verifiedToken)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            tokenService.validateEmailVerificationToken("verified-token")
        }
        assertEquals("Email already verified", exception.message)
    }

    @Test
    fun `validatePasswordResetToken should throw exception for used token`() {
        // Arrange
        val usedToken = PasswordResetToken(
            userId = UUID.randomUUID(),
            token = "used-token",
            expiresAt = Instant.now().plusSeconds(3600),
            usedAt = Instant.now()
        )
        whenever(passwordResetTokenRepository.findByToken("used-token")).thenReturn(usedToken)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            tokenService.validatePasswordResetToken("used-token")
        }
        assertEquals("Reset token has already been used", exception.message)
    }

    @Test
    fun `markEmailAsVerified should update verification token`() {
        // Arrange
        val token = "test-token"
        whenever(emailVerificationTokenRepository.markAsVerified(eq(token), any())).thenReturn(1)

        // Act
        tokenService.markEmailAsVerified(token)

        // Assert
        verify(emailVerificationTokenRepository).markAsVerified(eq(token), any())
    }

    @Test
    fun `markPasswordResetTokenAsUsed should update reset token`() {
        // Arrange
        val token = "test-token"
        whenever(passwordResetTokenRepository.markAsUsed(eq(token), any())).thenReturn(1)

        // Act
        tokenService.markPasswordResetTokenAsUsed(token)

        // Assert
        verify(passwordResetTokenRepository).markAsUsed(eq(token), any())
    }
}
