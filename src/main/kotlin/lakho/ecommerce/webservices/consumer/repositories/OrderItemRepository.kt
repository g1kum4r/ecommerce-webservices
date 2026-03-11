package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.OrderItem
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemRepository : CrudRepository<OrderItem, UUID> {

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY created_at ASC")
    fun findByOrderId(orderId: UUID): List<OrderItem>

    @Modifying
    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    fun deleteByOrderId(orderId: UUID)
}
