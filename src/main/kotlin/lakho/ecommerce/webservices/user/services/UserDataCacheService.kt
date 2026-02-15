package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

/**
 * Service for caching user data in Redis.
 * Implements Cache-Aside pattern for frequently accessed user information.
 *
 * Cache Strategy:
 * - On login: Cache all user data except password hash
 * - On read: Check cache first, load from DB on miss
 * - On update: Invalidate cache to force reload with fresh data
 * - On logout: Remove user data from cache
 *
 * Cache Keys:
 * - By User ID: "user:data:id:{userId}"
 * - By Email: "user:data:email:{email}"
 *
 * Both keys point to the same CachedUserData object for consistency.
 * TTL: 24 hours (refreshed on each access)
 */
@Service
class UserDataCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val userRepository: UserRepository,
    private val userRoleCacheService: UserRoleCacheService
) {
    private val logger = LoggerFactory.getLogger(UserDataCacheService::class.java)

    companion object {
        private const val USER_DATA_BY_ID_PREFIX = "user:data:id:"
        private const val USER_DATA_BY_EMAIL_PREFIX = "user:data:email:"
        private val CACHE_TTL = Duration.ofHours(24)
    }

    /**
     * Get cached user data by user ID.
     * Falls back to database if not in cache.
     */
    fun getUserDataById(userId: UUID): CachedUserData? {
        val cacheKey = "$USER_DATA_BY_ID_PREFIX$userId"

        // Try cache first
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            logger.debug("Cache hit for user data: userId={}", userId)
            return cachedData
        }

        // Cache miss - load from database
        logger.debug("Cache miss for user data: userId={}, loading from database", userId)
        return loadAndCacheUserData(userId)
    }

    /**
     * Get cached user data by email.
     * Falls back to database if not in cache.
     */
    fun getUserDataByEmail(email: String): CachedUserData? {
        val cacheKey = "$USER_DATA_BY_EMAIL_PREFIX$email"

        // Try cache first
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            logger.debug("Cache hit for user data: email={}", email)
            return cachedData
        }

        // Cache miss - load from database
        logger.debug("Cache miss for user data: email={}, loading from database", email)
        val user = userRepository.findByEmailOrUsername(email, email) ?: return null
        return loadAndCacheUserData(user.id!!)
    }

    /**
     * Cache user data with both ID and email keys.
     */
    fun cacheUserData(userData: CachedUserData) {
        val idKey = "$USER_DATA_BY_ID_PREFIX${userData.id}"
        val emailKey = "$USER_DATA_BY_EMAIL_PREFIX${userData.email}"

        try {
            // Cache with both keys for flexible lookup
            redisTemplate.opsForValue().set(idKey, userData, CACHE_TTL)
            redisTemplate.opsForValue().set(emailKey, userData, CACHE_TTL)
            logger.debug("Cached user data: userId={}, email={}", userData.id, userData.email)
        } catch (e: Exception) {
            logger.error("Failed to cache user data: userId={}, email={}", userData.id, userData.email, e)
        }
    }

    /**
     * Invalidate user data cache by user ID.
     * Removes both ID and email cache keys.
     */
    fun invalidateUserDataCache(userId: UUID) {
        try {
            // First get the cached data to find the email
            val cachedData = getCachedData("$USER_DATA_BY_ID_PREFIX$userId")
            val email = cachedData?.email

            // Delete ID-based cache
            val idDeleted = redisTemplate.delete("$USER_DATA_BY_ID_PREFIX$userId")
            if (idDeleted) {
                logger.info("Invalidated user data cache by ID: userId={}", userId)
            }

            // Delete email-based cache if email is known
            if (email != null) {
                val emailDeleted = redisTemplate.delete("$USER_DATA_BY_EMAIL_PREFIX$email")
                if (emailDeleted) {
                    logger.info("Invalidated user data cache by email: email={}", email)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate user data cache: userId={}", userId, e)
        }
    }

    /**
     * Invalidate user data cache by email.
     */
    fun invalidateUserDataCacheByEmail(email: String) {
        try {
            // First get the cached data to find the user ID
            val cachedData = getCachedData("$USER_DATA_BY_EMAIL_PREFIX$email")
            val userId = cachedData?.id

            // Delete email-based cache
            val emailDeleted = redisTemplate.delete("$USER_DATA_BY_EMAIL_PREFIX$email")
            if (emailDeleted) {
                logger.info("Invalidated user data cache by email: email={}", email)
            }

            // Delete ID-based cache if user ID is known
            if (userId != null) {
                val idDeleted = redisTemplate.delete("$USER_DATA_BY_ID_PREFIX$userId")
                if (idDeleted) {
                    logger.info("Invalidated user data cache by ID: userId={}", userId)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate user data cache: email={}", email, e)
        }
    }

    /**
     * Load user data from database and cache it.
     */
    private fun loadAndCacheUserData(userId: UUID): CachedUserData? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val roles = userRoleCacheService.getUserRoles(userId)

        val userData = CachedUserData(
            id = user.id!!,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            accountExpired = user.accountExpired,
            accountLocked = user.accountLocked,
            credentialsExpired = user.credentialsExpired,
            enabled = user.enabled,
            roles = roles
        )

        cacheUserData(userData)
        return userData
    }

    /**
     * Get cached data from Redis.
     */
    private fun getCachedData(cacheKey: String): CachedUserData? {
        return try {
            redisTemplate.opsForValue().get(cacheKey) as? CachedUserData
        } catch (e: Exception) {
            logger.error("Failed to retrieve cached user data: key={}", cacheKey, e)
            null
        }
    }
}
