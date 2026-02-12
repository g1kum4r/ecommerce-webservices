package lakho.ecommerce.webservices.auth

import lakho.ecommerce.webservices.auth.dto.AuthResponse
import lakho.ecommerce.webservices.auth.dto.LoginRequest
import lakho.ecommerce.webservices.auth.dto.RefreshRequest
import lakho.ecommerce.webservices.auth.dto.RegisterRequest
import lakho.ecommerce.webservices.auth.internal.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController internal constructor(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.register(request))

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(request))
}
