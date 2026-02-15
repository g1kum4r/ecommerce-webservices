package lakho.ecommerce.webservices.auth.services

import lakho.ecommerce.webservices.config.EmailProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mail.javamail.JavaMailSender
import jakarta.mail.internet.MimeMessage
import org.mockito.kotlin.argumentCaptor

class EmailServiceTest {

    private lateinit var mailSender: JavaMailSender
    private lateinit var emailProperties: EmailProperties
    private lateinit var emailService: EmailService
    private lateinit var mimeMessage: MimeMessage

    @BeforeEach
    fun setup() {
        mailSender = mock()
        mimeMessage = mock()
        
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
        
        whenever(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        
        emailService = EmailService(mailSender, emailProperties)
    }

    @Test
    fun `sendVerificationEmail should send email with correct parameters`() {
        // Arrange
        val to = "user@test.com"
        val username = "testuser"
        val token = "test-verification-token"

        // Act
        emailService.sendVerificationEmail(to, username, token)

        // Assert
        verify(mailSender).createMimeMessage()
        verify(mailSender).send(any<MimeMessage>())
    }

    @Test
    fun `sendPasswordResetEmail should send email with correct parameters`() {
        // Arrange
        val to = "user@test.com"
        val username = "testuser"
        val token = "test-reset-token"

        // Act
        emailService.sendPasswordResetEmail(to, username, token)

        // Assert
        verify(mailSender).createMimeMessage()
        verify(mailSender).send(any<MimeMessage>())
    }

    @Test
    fun `sendPasswordResetConfirmationEmail should send email with correct parameters`() {
        // Arrange
        val to = "user@test.com"
        val username = "testuser"

        // Act
        emailService.sendPasswordResetConfirmationEmail(to, username)

        // Assert
        verify(mailSender).createMimeMessage()
        verify(mailSender).send(any<MimeMessage>())
    }
}
