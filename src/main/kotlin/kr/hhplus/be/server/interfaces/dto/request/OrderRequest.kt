package kr.hhplus.be.server.interfaces.dto.request

data class OrderRequest(
    val customerId: Long,
    val items: List<OrderItemRequest>
)
