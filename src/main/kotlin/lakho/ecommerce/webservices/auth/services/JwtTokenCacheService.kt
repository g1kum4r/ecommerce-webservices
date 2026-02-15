package lakho.ecommerce.webservices.auth.services

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Service for caching JWT tokens in Redis.
 * Implements Cache-Aside pattern for token validation.
 *
 * Cache Strategy:
 * - On login: Cache both access and refresh tokens with their expiration times
 * - On authentication: Check if token exists in cache (fast validation)
 * - On logout: Remove tokens from cache to invalidate them
 *
 * Cache Keys:
 * - Access Token: "jwt:access:{token}"
 * - Refresh Token: "jwt:refresh:{token}"
 *
 * Cache Value: User email (or username) for quick user identification
 */
@Service
class JwtTokenCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val jwtService: JwtService
) {
    private val logger = LoggerFactory.getLogger(JwtTokenCacheService::class.java)

    companion object {
        private const val ACCESS_TOKEN_PREFIX = "jwt:access:"
        private const val REFRESH_TOKEN_PREFIX = "jwt:refresh:"
    }

    /**
     * Cache access token with expiration matching JWT expiration.
     * @param token The JWT access token
     * @param email User email/username
     * @param expirationTime Token expiration instant
     */
    fun cacheAccessToken(token: String, email: String, expirationTime: Instant) {
        val cacheKey = "$ACCESS_TOKEN_PREFIX$token"
        val ttl = Duration.between(Instant.now(), expirationTime)

        if (ttl.isNegative || ttl.isZero) {
            logger.warn("Attempted to cache expired access token: email={}", email)
            return
        }

        redisTemplate.opsForValue().set(cacheKey, email, ttl)
        logger.debug("Cached access token: email={}, ttl={}s", email, ttl.seconds)
    }

    /**
     * Cache refresh token with expiration matching JWT expiration.
     * @param token The JWT refresh token
     * @param email User email/username
     * @param expirationTime Token expiration instant
     */
    fun cacheRefreshToken(token: String, email: String, expirationTime: Instant) {
        val cacheKey = "$REFRESH_TOKEN_PREFIX$token"
        val ttl = Duration.between(Instant.now(), expirationTime)

        if (ttl.isNegative || ttl.isZero) {
            logger.warn("Attempted to cache expired refresh token: email={}", email)
            return
        }

        redisTemplate.opsForValue().set(cacheKey, email, ttl)
        logger.debug("Cached refresh token: email={}, ttl={}s", email, ttl.seconds)
    }

    /**
     * Check if access token exists in cache.
     * @param token The JWT access token
     * @return true if token exists in cache (not expired/revoked), false otherwise
     */
    fun isAccessTokenCached(token: String): Boolean {
        val cacheKey = "$ACCESS_TOKEN_PREFIX$token"
        val exists = redisTemplate.hasKey(cacheKey)
        logger.debug("Access token cache check: token exists={}", exists)
        return exists
    }

    /**
     * Check if refresh token exists in cache.
     * @param token The JWT refresh token
     * @return true if token exists in cache (not expired/revoked), false otherwise
     */
    fun isRefreshTokenCached(token: String): Boolean {
        val cacheKey = "$REFRESH_TOKEN_PREFIX$token"
        val exists = redisTemplate.hasKey(cacheKey)
        logger.debug("Refresh token cache check: token exists={}", exists)
        return exists
    }

    /**
     * Remove access token from cache (logout/revocation).
     * @param token The JWT access token
     */
    fun removeAccessToken(token: String) {
        val cacheKey = "$ACCESS_TOKEN_PREFIX$token"
        val deleted = redisTemplate.delete(cacheKey)
        if (deleted) {
            logger.info("Removed access token from cache")
        } else {
            logger.warn("Attempted to remove non-existent access token from cache")
        }
    }

    /**
     * Remove refresh token from cache (logout/revocation).
     * @param token The JWT refresh token
     */
    fun removeRefreshToken(token: String) {
        val cacheKey = "$REFRESH_TOKEN_PREFIX$token"
        val deleted = redisTemplate.delete(cacheKey)
        if (deleted) {
            logger.info("Removed refresh token from cache")
        } else {
            logger.warn("Attempted to remove non-existent refresh token from cache")
        }
    }

    /**
     * Get user email from cached access token.
     * @param token The JWT access token
     * @return User email or null if token not found in cache
     */
    fun getEmailFromAccessToken(token: String): String? {
        val cacheKey = "$ACCESS_TOKEN_PREFIX$token"
        return redisTemplate.opsForValue().get(cacheKey) as? String
    }

    /**
     * Cache both access and refresh tokens.
     * Convenience method for login flow.
     */
    fun cacheTokens(accessToken: String, refreshToken: String, email: String) {
        val accessExpiration = jwtService.extractExpiration(accessToken)
        val refreshExpiration = jwtService.extractExpiration(refreshToken)

        if (accessExpiration != null) {
            cacheAccessToken(accessToken, email, accessExpiration)
        } else {
            logger.error("Failed to extract expiration from access token: email={}", email)
        }

        if (refreshExpiration != null) {
            cacheRefreshToken(refreshToken, email, refreshExpiration)
        } else {
            logger.error("Failed to extract expiration from refresh token: email={}", email)
        }
    }

    /**
     * Remove both access and refresh tokens from cache.
     * Used during logout to invalidate all tokens.
     */
    fun removeTokens(accessToken: String?, refreshToken: String?) {
        accessToken?.let { removeAccessToken(it) }
        refreshToken?.let { removeRefreshToken(it) }
        logger.info("Removed tokens from cache during logout")
    }
}
