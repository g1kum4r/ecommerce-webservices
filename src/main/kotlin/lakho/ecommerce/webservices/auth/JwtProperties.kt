package lakho.ecommerce.webservices.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)
