package kr.hhplus.be.server.interfaces.dto.response

data class OrderResponse(
    val orderId: Long,
    val customerId: Long,
    val totalPrice: Int,
    val createdAt: String,
    val items: List<OrderItemResponse>
)