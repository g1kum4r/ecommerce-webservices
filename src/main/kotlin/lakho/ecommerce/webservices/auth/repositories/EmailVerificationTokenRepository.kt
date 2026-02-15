package lakho.ecommerce.webservices.auth.repositories

import lakho.ecommerce.webservices.auth.repositories.entities.EmailVerificationToken
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface EmailVerificationTokenRepository : CrudRepository<EmailVerificationToken, UUID> {
    
    fun findByToken(token: String): EmailVerificationToken?
    
    fun findByUserId(userId: UUID): List<EmailVerificationToken>
    
    @Modifying
    @Query("UPDATE email_verification_tokens SET verified_at = :verifiedAt WHERE token = :token")
    fun markAsVerified(@Param("token") token: String, @Param("verifiedAt") verifiedAt: Instant): Int
    
    @Modifying
    @Query("DELETE FROM email_verification_tokens WHERE user_id = :userId")
    fun deleteByUserId(@Param("userId") userId: UUID): Int
    
    @Modifying
    @Query("DELETE FROM email_verification_tokens WHERE expires_at < :now")
    fun deleteExpiredTokens(@Param("now") now: Instant): Int
}
