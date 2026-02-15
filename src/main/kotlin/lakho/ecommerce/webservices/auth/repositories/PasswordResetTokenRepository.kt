package lakho.ecommerce.webservices.auth.repositories

import lakho.ecommerce.webservices.auth.repositories.entities.PasswordResetToken
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface PasswordResetTokenRepository : CrudRepository<PasswordResetToken, UUID> {
    
    fun findByToken(token: String): PasswordResetToken?
    
    fun findByUserId(userId: UUID): List<PasswordResetToken>
    
    @Modifying
    @Query("UPDATE password_reset_tokens SET used_at = :usedAt WHERE token = :token")
    fun markAsUsed(@Param("token") token: String, @Param("usedAt") usedAt: Instant): Int
    
    @Modifying
    @Query("DELETE FROM password_reset_tokens WHERE user_id = :userId")
    fun deleteByUserId(@Param("userId") userId: UUID): Int
    
    @Modifying
    @Query("DELETE FROM password_reset_tokens WHERE expires_at < :now")
    fun deleteExpiredTokens(@Param("now") now: Instant): Int
}
