package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.auth.repositories.EmailVerificationTokenRepository
import lakho.ecommerce.webservices.auth.repositories.PasswordResetTokenRepository
import lakho.ecommerce.webservices.auth.repositories.entities.EmailVerificationToken
import lakho.ecommerce.webservices.auth.repositories.entities.PasswordResetToken
import lakho.ecommerce.webservices.config.EmailProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class TokenService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailProperties: EmailProperties
) {

    private val secureRandom = SecureRandom()

    @Transactional
    fun createEmailVerificationToken(userId: UUID): EmailVerificationToken {
        // Delete any existing verification tokens for this user
        emailVerificationTokenRepository.deleteByUserId(userId)

        val token = generateSecureToken()
        val expiresAt = Instant.now().plusSeconds(emailProperties.emailVerificationTokenExpirationHours * 3600)

        val verificationToken = EmailVerificationToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        )

        return emailVerificationTokenRepository.save(verificationToken)
    }

    @Transactional
    fun createPasswordResetToken(userId: UUID): PasswordResetToken {
        // Delete any existing password reset tokens for this user
        passwordResetTokenRepository.deleteByUserId(userId)

        val token = generateSecureToken()
        val expiresAt = Instant.now().plusSeconds(emailProperties.passwordResetTokenExpirationHours * 3600)

        val resetToken = PasswordResetToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        )

        return passwordResetTokenRepository.save(resetToken)
    }

    fun validateEmailVerificationToken(token: String): EmailVerificationToken {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid verification token")

        if (verificationToken.isVerified()) {
            throw IllegalArgumentException("Email already verified")
        }

        if (verificationToken.isExpired()) {
            throw IllegalArgumentException("Verification token has expired")
        }

        return verificationToken
    }

    fun validatePasswordResetToken(token: String): PasswordResetToken {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid reset token")

        if (resetToken.isUsed()) {
            throw IllegalArgumentException("Reset token has already been used")
        }

        if (resetToken.isExpired()) {
            throw IllegalArgumentException("Reset token has expired")
        }

        return resetToken
    }

    @Transactional
    fun markEmailAsVerified(token: String) {
        emailVerificationTokenRepository.markAsVerified(token, Instant.now())
    }

    @Transactional
    fun markPasswordResetTokenAsUsed(token: String) {
        passwordResetTokenRepository.markAsUsed(token, Instant.now())
    }

    @Transactional
    fun cleanupExpiredTokens() {
        val now = Instant.now()
        val deletedVerificationTokens = emailVerificationTokenRepository.deleteExpiredTokens(now)
        val deletedResetTokens = passwordResetTokenRepository.deleteExpiredTokens(now)
        
        if (deletedVerificationTokens > 0 || deletedResetTokens > 0) {
            println("Cleaned up $deletedVerificationTokens expired verification tokens and $deletedResetTokens expired reset tokens")
        }
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
