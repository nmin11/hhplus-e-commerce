package kr.hhplus.be.server.dto

data class OrderItemRequest(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Int
)
