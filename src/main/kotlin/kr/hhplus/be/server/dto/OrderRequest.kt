package kr.hhplus.be.server.dto

data class OrderRequest(
    val customerId: Long,
    val items: List<OrderItemRequest>
)
