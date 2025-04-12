package kr.hhplus.be.server.application.dataplatform

sealed class DataPlatformCommand {
    data class Order(
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
