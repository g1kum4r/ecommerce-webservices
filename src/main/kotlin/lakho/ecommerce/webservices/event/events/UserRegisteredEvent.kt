package lakho.ecommerce.webservices.event.events

import org.springframework.context.ApplicationEvent
import java.util.*

class UserRegisteredEvent(
    source: Any,
    val userId: UUID,
    val email: String,
    val username: String,
    val verificationToken: String
) : ApplicationEvent(source)
