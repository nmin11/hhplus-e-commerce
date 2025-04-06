package kr.hhplus.be.server.interfaces.dto.request

data class OrderItemRequest(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Int
)
