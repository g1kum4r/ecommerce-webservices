package lakho.ecommerce.webservices.auth.events

import org.springframework.context.ApplicationEvent

/**
 * Event published when a user successfully logs in.
 * Used to cache JWT tokens in Redis for fast validation.
 *
 * @property email User email/username
 * @property accessToken JWT access token
 * @property refreshToken JWT refresh token
 */
class LoginSuccessEvent(
    source: Any,
    val email: String,
    val accessToken: String,
    val refreshToken: String
) : ApplicationEvent(source)
