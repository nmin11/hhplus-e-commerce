package kr.hhplus.be.server.interfaces.order

sealed class OrderResponse {
    data class Create(
        val orderId: Long,
        val customerId: Long,
        val totalPrice: Int,
        val createdAt: String,
        val items: List<OrderItem>
    )

    data class OrderItem(
        val productName: String,
        val optionName: String,
        val quantity: Int,
        val subtotalPrice: Int
    )
}
