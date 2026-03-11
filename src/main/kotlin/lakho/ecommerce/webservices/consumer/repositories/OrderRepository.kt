package lakho.ecommerce.webservices.consumer.repositories

import lakho.ecommerce.webservices.consumer.repositories.entities.Order
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : CrudRepository<Order, UUID> {

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByUserId(userId: UUID, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId")
    fun countByUserId(userId: UUID): Long

    @Query("SELECT * FROM orders WHERE user_id = :userId AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByUserIdAndStatus(userId: UUID, status: String, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId AND status = :status")
    fun countByUserIdAndStatus(userId: UUID, status: String): Long

    @Query("SELECT * FROM orders WHERE id = :id AND user_id = :userId")
    fun findByIdAndUserId(id: UUID, userId: UUID): Order?

    @Query("SELECT * FROM orders WHERE order_number = :orderNumber")
    fun findByOrderNumber(orderNumber: String): Order?

    @Query("SELECT * FROM orders WHERE store_id = :storeId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByStoreId(storeId: UUID, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId")
    fun countByStoreId(storeId: UUID): Long

    @Query("SELECT * FROM orders WHERE store_id = :storeId AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByStoreIdAndStatus(storeId: UUID, status: String, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = :status")
    fun countByStoreIdAndStatus(storeId: UUID, status: String): Long

    @Query("SELECT * FROM orders WHERE id = :id AND store_id = :storeId")
    fun findByIdAndStoreId(id: UUID, storeId: UUID): Order?

    @Query("SELECT * FROM orders ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllOrders(limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders")
    fun countAllOrders(): Long

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllByStatus(status: String, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    fun countAllByStatus(status: String): Long

    @Query("SELECT * FROM orders WHERE store_id = :storeId AND status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllByStoreIdAndStatus(storeId: UUID, status: String, limit: Int, offset: Int): List<Order>

    @Query("SELECT * FROM orders WHERE store_id = :storeId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllByStoreId(storeId: UUID, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId")
    fun countAllByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = :status")
    fun countAllByStoreIdAndStatus(storeId: UUID, status: String): Long

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllByUserId(userId: UUID, limit: Int, offset: Int): List<Order>

    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId")
    fun countAllByUserId(userId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'PENDING'")
    fun countPendingByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'CONFIRMED'")
    fun countConfirmedByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'PROCESSING'")
    fun countProcessingByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'SHIPPED'")
    fun countShippedByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'DELIVERED'")
    fun countDeliveredByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'COMPLETED'")
    fun countCompletedByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE store_id = :storeId AND status = 'CANCELLED'")
    fun countCancelledByStoreId(storeId: UUID): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'")
    fun countAllPending(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'CONFIRMED'")
    fun countAllConfirmed(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PROCESSING'")
    fun countAllProcessing(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'SHIPPED'")
    fun countAllShipped(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'")
    fun countAllDelivered(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
    fun countAllCompleted(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED'")
    fun countAllCancelled(): Long
}
