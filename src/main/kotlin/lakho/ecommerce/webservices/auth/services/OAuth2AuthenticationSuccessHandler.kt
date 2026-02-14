package lakho.ecommerce.webservices.auth.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.services.RoleService
import lakho.ecommerce.webservices.user.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationSuccessHandler(
    private val userService: UserService,
    private val roleService: RoleService,
    private val jwtService: JwtService
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val email = oauth2User.getAttribute<String>("email")
            ?: throw IllegalStateException("Email not found in OAuth2 response")

        val name = oauth2User.getAttribute<String>("name") ?: ""
        val givenName = oauth2User.getAttribute<String>("given_name")
        val familyName = oauth2User.getAttribute<String>("family_name")

        // Check if user already exists
        var user = userService.findByEmailOrUsername(email)

        if (user == null) {
            // Register new user with CONSUMER role (OAuth2 users don't have passwords)
            val role = roleService.findByRoleName(Roles.CONSUMER.name)
            user = userService.save(
                User(
                    email = email,
                    username = email,
                    passwordHash = null,
                    firstName = givenName,
                    lastName = familyName
                ),
                setOf(Roles.CONSUMER)
            )
        }

        // Generate JWT tokens
        val roles = user.roles.joinToString(",") { it.name }
        val accessToken = jwtService.generateAccessToken(user.email, roles)
        val refreshToken = jwtService.generateRefreshToken(user.email, roles)

        // Redirect to frontend with tokens
        val redirectUrl = String.format(
            "/oauth2/redirect?accessToken=%s&refreshToken=%s",
            accessToken,
            refreshToken
        )

        setDefaultTargetUrl(redirectUrl)
        super.onAuthenticationSuccess(request, response, authentication)
    }
}
