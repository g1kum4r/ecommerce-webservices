package lakho.ecommerce.webservices

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<WebservicesApplication>().with(TestcontainersConfiguration::class).run(*args)
}
