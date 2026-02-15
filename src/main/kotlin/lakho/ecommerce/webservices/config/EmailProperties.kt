package lakho.ecommerce.webservices.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class EmailProperties(
    val mail: MailSettings,
    val baseUrl: String,
    val frontendUrl: String,
    val emailVerificationTokenExpirationHours: Long,
    val passwordResetTokenExpirationHours: Long
) {
    data class MailSettings(
        val from: String,
        val fromName: String
    )
}
