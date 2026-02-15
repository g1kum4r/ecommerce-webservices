package lakho.ecommerce.webservices.auth.events

import lakho.ecommerce.webservices.user.repositories.models.User
import org.springframework.context.ApplicationEvent

/**
 * Event published when a user successfully logs in.
 * Used to cache JWT tokens and user data in Redis for fast validation.
 *
 * @property user User model with complete data
 * @property accessToken JWT access token
 * @property refreshToken JWT refresh token
 */
class LoginSuccessEvent(
    source: Any,
    val user: User,
    val accessToken: String,
    val refreshToken: String
) : ApplicationEvent(source)
