package lakho.ecommerce.webservices.consumer.api.models

data class OrderStatsResponse(
    val total: Long,
    val pending: Long,
    val confirmed: Long,
    val processing: Long,
    val shipped: Long,
    val delivered: Long,
    val completed: Long,
    val cancelled: Long
)
