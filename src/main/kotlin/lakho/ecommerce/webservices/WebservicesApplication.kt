package lakho.ecommerce.webservices

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class WebservicesApplication

fun main(args: Array<String>) {
    runApplication<WebservicesApplication>(*args)
}
