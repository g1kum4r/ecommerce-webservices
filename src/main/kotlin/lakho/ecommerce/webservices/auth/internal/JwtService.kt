package lakho.ecommerce.webservices.auth.internal

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import lakho.ecommerce.webservices.auth.JwtProperties
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
internal class JwtService(private val jwtProperties: JwtProperties) {

    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateAccessToken(email: String, role: String): String =
        generateToken(email, role, jwtProperties.accessTokenExpirationMs)

    fun generateRefreshToken(email: String, role: String): String =
        generateToken(email, role, jwtProperties.refreshTokenExpirationMs)

    fun extractEmail(token: String): String? =
        extractClaims(token)?.subject

    fun extractRole(token: String): String? =
        extractClaims(token)?.get("role", String::class.java)

    fun isTokenValid(token: String): Boolean {
        val claims = extractClaims(token) ?: return false
        return claims.expiration.after(Date())
    }

    private fun generateToken(email: String, role: String, expirationMs: Long): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    private fun extractClaims(token: String): Claims? =
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
}
