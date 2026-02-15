package lakho.ecommerce.webservices.config.security.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lakho.ecommerce.webservices.auth.services.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

internal class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            if (jwtService.isTokenValid(token)) {
                val email = jwtService.extractEmail(token)
                val rolesString = jwtService.extractRole(token)
                if (email != null && rolesString != null) {
                    val authorities = rolesString.split(",")
                        .map { SimpleGrantedAuthority("ROLE_$it") }
                    val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}