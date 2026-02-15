package lakho.ecommerce.webservices.config.security.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lakho.ecommerce.webservices.auth.services.JwtService
import lakho.ecommerce.webservices.auth.services.JwtTokenCacheService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

internal class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val jwtTokenCacheService: JwtTokenCacheService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)

            // First check if token exists in Redis cache (fast check for revoked/expired tokens)
            if (!jwtTokenCacheService.isAccessTokenCached(token)) {
                logger.warn("Token not found in cache (expired or revoked): remote_addr={}", request.remoteAddr)
                response.status = HttpStatus.UNAUTHORIZED.value()
                response.writer.write("{\"error\": \"Token expired or invalid\"}")
                response.contentType = "application/json"
                return
            }

            // Token exists in cache, now validate JWT signature and claims
            if (jwtService.isTokenValid(token)) {
                val email = jwtService.extractEmail(token)
                val rolesString = jwtService.extractRole(token)
                if (email != null && rolesString != null) {
                    val authorities = rolesString.split(",")
                        .map { SimpleGrantedAuthority("ROLE_$it") }
                    val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("Authentication successful: email={}", email)
                }
            } else {
                logger.warn("Invalid token signature: remote_addr={}", request.remoteAddr)
                response.status = HttpStatus.UNAUTHORIZED.value()
                response.writer.write("{\"error\": \"Invalid token\"}")
                response.contentType = "application/json"
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}