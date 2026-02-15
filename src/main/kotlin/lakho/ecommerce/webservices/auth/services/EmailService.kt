package lakho.ecommerce.webservices.auth.services

import jakarta.mail.internet.MimeMessage
import lakho.ecommerce.webservices.config.EmailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val emailProperties: EmailProperties
) {

    fun sendVerificationEmail(to: String, username: String, token: String) {
        val verificationUrl = "${emailProperties.frontendUrl}/verify-email?token=$token"
        
        val subject = "Verify Your Email - ${emailProperties.mail.fromName}"
        val body = buildEmailVerificationBody(username, verificationUrl)
        
        sendEmail(to, subject, body)
    }

    fun sendPasswordResetEmail(to: String, username: String, token: String) {
        val resetUrl = "${emailProperties.frontendUrl}/reset-password?token=$token"
        
        val subject = "Password Reset Request - ${emailProperties.mail.fromName}"
        val body = buildPasswordResetBody(username, resetUrl)
        
        sendEmail(to, subject, body)
    }

    fun sendPasswordResetConfirmationEmail(to: String, username: String) {
        val subject = "Password Successfully Reset - ${emailProperties.mail.fromName}"
        val body = buildPasswordResetConfirmationBody(username)
        
        sendEmail(to, subject, body)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        
        helper.setFrom(emailProperties.mail.from, emailProperties.mail.fromName)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, true)
        
        mailSender.send(message)
    }

    private fun buildEmailVerificationBody(username: String, verificationUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; border-radius: 10px; padding: 30px;">
                    <h1 style="color: #007bff; margin-bottom: 20px;">Welcome to ${emailProperties.mail.fromName}!</h1>
                    
                    <p>Hello <strong>$username</strong>,</p>
                    
                    <p>Thank you for registering with us. To complete your registration and activate your account, please verify your email address by clicking the button below:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$verificationUrl" 
                           style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                            Verify Email Address
                        </a>
                    </div>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #007bff;">$verificationUrl</p>
                    
                    <p><strong>This link will expire in ${emailProperties.emailVerificationTokenExpirationHours} hours.</strong></p>
                    
                    <p>If you didn't create an account with us, you can safely ignore this email.</p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #666;">
                        This is an automated email, please do not reply.<br>
                        © 2025 ${emailProperties.mail.fromName}. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildPasswordResetBody(username: String, resetUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; border-radius: 10px; padding: 30px;">
                    <h1 style="color: #dc3545; margin-bottom: 20px;">Password Reset Request</h1>
                    
                    <p>Hello <strong>$username</strong>,</p>
                    
                    <p>We received a request to reset your password. Click the button below to create a new password:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$resetUrl" 
                           style="background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                            Reset Password
                        </a>
                    </div>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #dc3545;">$resetUrl</p>
                    
                    <p><strong>This link will expire in ${emailProperties.passwordResetTokenExpirationHours} hour(s).</strong></p>
                    
                    <p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #666;">
                        This is an automated email, please do not reply.<br>
                        © 2025 ${emailProperties.mail.fromName}. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildPasswordResetConfirmationBody(username: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; border-radius: 10px; padding: 30px;">
                    <h1 style="color: #28a745; margin-bottom: 20px;">Password Successfully Reset</h1>
                    
                    <p>Hello <strong>$username</strong>,</p>
                    
                    <p>Your password has been successfully reset. You can now log in with your new password.</p>
                    
                    <p>If you didn't make this change or if you believe an unauthorized person has accessed your account, please contact our support team immediately.</p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #666;">
                        This is an automated email, please do not reply.<br>
                        © 2025 ${emailProperties.mail.fromName}. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
