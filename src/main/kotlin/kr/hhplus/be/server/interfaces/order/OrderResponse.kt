package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.domain.order.Order

sealed class OrderResponse {
    data class Create(
        val orderId: Long,
        val customerId: Long,
        val totalPrice: Int,
        val createdAt: String,
        val items: List<OrderItem>
    ) {
        companion object {
            fun from(order: Order): Create {
                return Create(
                    orderId = order.requireSavedId(),
                    customerId = order.customer.requireSavedId(),
                    totalPrice = order.totalPrice,
                    createdAt = order.createdAt.toString(),
                    items = order.orderItems.map {
                        OrderItem(
                            productName = it.productOption.product.name,
                            optionName = it.productOption.optionName,
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
