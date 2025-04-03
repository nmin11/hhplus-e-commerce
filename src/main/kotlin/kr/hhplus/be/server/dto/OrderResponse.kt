package kr.hhplus.be.server.dto

data class OrderResponse(
    val orderId: Long,
    val customerId: Long,
    val totalPrice: Int,
    val createdAt: String,
    val items: List<OrderItemResponse>
)
