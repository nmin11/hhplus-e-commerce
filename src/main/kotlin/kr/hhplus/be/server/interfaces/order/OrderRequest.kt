package kr.hhplus.be.server.interfaces.order

sealed class OrderRequest {
    data class Create(
        val customerId: Long,
        val items: List<OrderItem>
    )

    data class OrderItem(
        val productId: Long,
        val productOptionId: Long,
        val quantity: Int
    )
}
