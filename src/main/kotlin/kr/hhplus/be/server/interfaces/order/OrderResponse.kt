package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.application.order.OrderResult

sealed class OrderResponse {
    data class Create(
        val orderId: Long,
        val customerId: Long,
        val totalPrice: Int,
        val createdAt: String,
        val items: List<OrderItem>
    ) {
        companion object {
            fun from(result: OrderResult.Create): Create {
                return Create(
                    orderId = result.orderId,
                    customerId = result.customerId,
                    totalPrice = result.totalPrice,
                    createdAt = result.createdAt,
                    items = result.items.map {
                        OrderItem(
                            productName = it.productName,
                            optionName = it.optionName,
                            quantity = it.quantity,
                            subtotalPrice = it.subtotalPrice
                        )
                    }
                )
            }
        }
    }

    data class OrderItem(
        val productName: String,
        val optionName: String,
        val quantity: Int,
        val subtotalPrice: Int
    )
}
