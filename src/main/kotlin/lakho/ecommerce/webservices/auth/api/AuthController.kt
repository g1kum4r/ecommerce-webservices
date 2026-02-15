package lakho.ecommerce.webservices.auth.api

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.ForgotPasswordRequest
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
import lakho.ecommerce.webservices.auth.api.models.ResetPasswordRequest
import lakho.ecommerce.webservices.auth.api.models.VerifyEmailRequest
import lakho.ecommerce.webservices.auth.services.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController internal constructor(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(request))

    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<Map<String, String>> {
        authService.verifyEmail(request.token)
        return ResponseEntity.ok(mapOf("message" to "Email verified successfully"))
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<Map<String, String>> {
        authService.forgotPassword(request.email)
        return ResponseEntity.ok(mapOf("message" to "Password reset email sent"))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<Map<String, String>> {
        authService.resetPassword(request.token, request.newPassword)
        return ResponseEntity.ok(mapOf("message" to "Password reset successfully"))
    }

    @PostMapping("/logout")
    fun logout(httpRequest: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val authHeader = httpRequest.getHeader("Authorization")
        val token = if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            null
        }

        authService.logout(token)
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}
