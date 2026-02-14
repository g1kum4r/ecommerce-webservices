package lakho.ecommerce.webservices.auth.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.auth.api.models.AuthResponse
import lakho.ecommerce.webservices.auth.api.models.LoginRequest
import lakho.ecommerce.webservices.auth.api.models.RefreshRequest
import lakho.ecommerce.webservices.auth.api.models.RegisterRequest
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
}
