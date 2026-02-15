package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.repositories.RoleRepository
import lakho.ecommerce.webservices.user.repositories.entities.Role
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

/**
 * Cache-Aside Pattern implementation for User Roles.
 *
 * This service manages caching of user roles in Redis with automatic cache invalidation
 * on role updates. It implements the Cache-Aside (Lazy Loading) pattern:
 *
 * 1. Read: Check cache first, if miss then load from DB and populate cache
 * 2. Write: Update DB first, then invalidate cache (write-through handled by event listeners)
 *
 * Benefits:
 * - Reduces database load for frequent role checks during authentication
 * - Automatic cache invalidation on role updates via transaction events
 * - TTL-based expiration for cache consistency
 */
@Service
class UserRoleCacheService(
    private val roleRepository: RoleRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(UserRoleCacheService::class.java)

    companion object {
        private const val USER_ROLES_CACHE_PREFIX = "user:roles:"
        private val CACHE_TTL = Duration.ofHours(24)
    }

    /**
     * Get user roles using Cache-Aside pattern.
     *
     * Flow:
     * 1. Try to get from cache
     * 2. If cache miss, load from database
     * 3. Populate cache
     * 4. Return roles
     */
    fun getUserRoles(userId: UUID): Set<Role> {
        val cacheKey = "$USER_ROLES_CACHE_PREFIX$userId"

        // Try cache first (Cache Hit)
        val cachedRoles = getCachedRoles(cacheKey)
        if (cachedRoles != null) {
            logger.debug("Cache hit for user roles: userId={}", userId)
            return cachedRoles
        }

        // Cache miss - load from database
        logger.debug("Cache miss for user roles: userId={}, loading from database", userId)
        val roles = roleRepository.findByUserId(userId).toSet()

        // Populate cache for next time
        cacheUserRoles(userId, roles)

        return roles
    }

    /**
     * Cache user roles after login or role assignment.
     */
    fun cacheUserRoles(userId: UUID, roles: Set<Role>) {
        val cacheKey = "$USER_ROLES_CACHE_PREFIX$userId"

        try {
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_TTL)
            logger.debug("Cached roles for user: userId={}, roles={}", userId, roles.map { it.name })
        } catch (e: Exception) {
            logger.error("Failed to cache user roles: userId={}", userId, e)
            // Don't throw - caching failure shouldn't break the flow
        }
    }

    /**
     * Invalidate user roles cache.
     * Called when user roles are updated.
     */
    fun invalidateUserRolesCache(userId: UUID) {
        val cacheKey = "$USER_ROLES_CACHE_PREFIX$userId"

        try {
            val deleted = redisTemplate.delete(cacheKey)
            if (deleted) {
                logger.info("Invalidated role cache for user: userId={}", userId)
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate user roles cache: userId={}", userId, e)
        }
    }

    /**
     * Invalidate cache for multiple users.
     * Useful when a role itself is modified affecting multiple users.
     */
    fun invalidateMultipleUserRolesCache(userIds: Set<UUID>) {
        userIds.forEach { invalidateUserRolesCache(it) }
        logger.info("Invalidated role cache for {} users", userIds.size)
    }

    /**
     * Clear all user roles cache.
     * Use with caution - should only be used for admin operations or cache corruption recovery.
     */
    fun clearAllUserRolesCache() {
        try {
            val keys = redisTemplate.keys("$USER_ROLES_CACHE_PREFIX*")
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                logger.warn("Cleared all user roles cache: {} keys deleted", keys.size)
            }
        } catch (e: Exception) {
            logger.error("Failed to clear all user roles cache", e)
        }
    }

    /**
     * Get cached roles if available.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getCachedRoles(cacheKey: String): Set<Role>? {
        return try {
            redisTemplate.opsForValue().get(cacheKey) as? Set<Role>
        } catch (e: Exception) {
            logger.error("Error reading from cache: key={}", cacheKey, e)
            null
        }
    }
}
