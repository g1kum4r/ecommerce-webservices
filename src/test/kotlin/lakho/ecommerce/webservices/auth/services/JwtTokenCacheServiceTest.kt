package lakho.ecommerce.webservices.auth.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.time.Instant

class JwtTokenCacheServiceTest {

    private lateinit var redisTemplate: RedisTemplate<String, Any>
    private lateinit var valueOperations: ValueOperations<String, Any>
    private lateinit var jwtService: JwtService
    private lateinit var jwtTokenCacheService: JwtTokenCacheService

    @BeforeEach
    fun setup() {
        redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, Any>
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, Any>
        jwtService = mock(JwtService::class.java)

        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        jwtTokenCacheService = JwtTokenCacheService(redisTemplate, jwtService)
    }

    @Test
    fun `cacheAccessToken should cache token with correct TTL`() {
        // Arrange
        val token = "test-access-token"
        val email = "test@example.com"
        val expirationTime = Instant.now().plusSeconds(900) // 15 minutes

        // Act
        jwtTokenCacheService.cacheAccessToken(token, email, expirationTime)

        // Assert
        verify(valueOperations).set(
            eq("jwt:access:$token"),
            eq(email),
            any(Duration::class.java)
        )
    }

    @Test
    fun `cacheRefreshToken should cache token with correct TTL`() {
        // Arrange
        val token = "test-refresh-token"
        val email = "test@example.com"
        val expirationTime = Instant.now().plusSeconds(604800) // 7 days

        // Act
        jwtTokenCacheService.cacheRefreshToken(token, email, expirationTime)

        // Assert
        verify(valueOperations).set(
            eq("jwt:refresh:$token"),
            eq(email),
            any(Duration::class.java)
        )
    }

    @Test
    fun `cacheAccessToken should not cache expired token`() {
        // Arrange
        val token = "expired-token"
        val email = "test@example.com"
        val expirationTime = Instant.now().minusSeconds(60) // Already expired

        // Act
        jwtTokenCacheService.cacheAccessToken(token, email, expirationTime)

        // Assert
        verify(valueOperations, never()).set(anyString(), any(), any(Duration::class.java))
    }

    @Test
    fun `isAccessTokenCached should return true when token exists`() {
        // Arrange
        val token = "existing-token"
        `when`(redisTemplate.hasKey("jwt:access:$token")).thenReturn(true)

        // Act
        val result = jwtTokenCacheService.isAccessTokenCached(token)

        // Assert
        assertTrue(result)
        verify(redisTemplate).hasKey("jwt:access:$token")
    }

    @Test
    fun `isAccessTokenCached should return false when token does not exist`() {
        // Arrange
        val token = "non-existing-token"
        `when`(redisTemplate.hasKey("jwt:access:$token")).thenReturn(false)

        // Act
        val result = jwtTokenCacheService.isAccessTokenCached(token)

        // Assert
        assertFalse(result)
        verify(redisTemplate).hasKey("jwt:access:$token")
    }

    @Test
    fun `isRefreshTokenCached should return true when token exists`() {
        // Arrange
        val token = "existing-refresh-token"
        `when`(redisTemplate.hasKey("jwt:refresh:$token")).thenReturn(true)

        // Act
        val result = jwtTokenCacheService.isRefreshTokenCached(token)

        // Assert
        assertTrue(result)
        verify(redisTemplate).hasKey("jwt:refresh:$token")
    }

    @Test
    fun `removeAccessToken should delete token from cache`() {
        // Arrange
        val token = "token-to-remove"
        `when`(redisTemplate.delete("jwt:access:$token")).thenReturn(true)

        // Act
        jwtTokenCacheService.removeAccessToken(token)

        // Assert
        verify(redisTemplate).delete("jwt:access:$token")
    }

    @Test
    fun `removeRefreshToken should delete token from cache`() {
        // Arrange
        val token = "refresh-token-to-remove"
        `when`(redisTemplate.delete("jwt:refresh:$token")).thenReturn(true)

        // Act
        jwtTokenCacheService.removeRefreshToken(token)

        // Assert
        verify(redisTemplate).delete("jwt:refresh:$token")
    }

    @Test
    fun `getEmailFromAccessToken should return email when token exists`() {
        // Arrange
        val token = "valid-token"
        val email = "user@example.com"
        `when`(valueOperations.get("jwt:access:$token")).thenReturn(email)

        // Act
        val result = jwtTokenCacheService.getEmailFromAccessToken(token)

        // Assert
        assertEquals(email, result)
        verify(valueOperations).get("jwt:access:$token")
    }

    @Test
    fun `getEmailFromAccessToken should return null when token does not exist`() {
        // Arrange
        val token = "non-existing-token"
        `when`(valueOperations.get("jwt:access:$token")).thenReturn(null)

        // Act
        val result = jwtTokenCacheService.getEmailFromAccessToken(token)

        // Assert
        assertNull(result)
        verify(valueOperations).get("jwt:access:$token")
    }

    @Test
    fun `cacheTokens should cache both access and refresh tokens`() {
        // Arrange
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val email = "test@example.com"
        val accessExpiration = Instant.now().plusSeconds(900)
        val refreshExpiration = Instant.now().plusSeconds(604800)

        `when`(jwtService.extractExpiration(accessToken)).thenReturn(accessExpiration)
        `when`(jwtService.extractExpiration(refreshToken)).thenReturn(refreshExpiration)

        // Act
        jwtTokenCacheService.cacheTokens(accessToken, refreshToken, email)

        // Assert
        verify(jwtService).extractExpiration(accessToken)
        verify(jwtService).extractExpiration(refreshToken)
        verify(valueOperations, times(2)).set(anyString(), eq(email), any(Duration::class.java))
    }

    @Test
    fun `removeTokens should remove both access and refresh tokens`() {
        // Arrange
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        `when`(redisTemplate.delete(anyString())).thenReturn(true)

        // Act
        jwtTokenCacheService.removeTokens(accessToken, refreshToken)

        // Assert
        verify(redisTemplate).delete("jwt:access:$accessToken")
        verify(redisTemplate).delete("jwt:refresh:$refreshToken")
    }

    @Test
    fun `removeTokens should handle null tokens gracefully`() {
        // Act
        jwtTokenCacheService.removeTokens(null, null)

        // Assert
        verify(redisTemplate, never()).delete(anyString())
    }
}
