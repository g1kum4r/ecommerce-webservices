package lakho.ecommerce.webservices.event.events

import org.springframework.context.ApplicationEvent
import java.util.*

class PasswordResetEvent(
    source: Any,
    val userId: UUID,
    val email: String,
    val username: String
) : ApplicationEvent(source)
