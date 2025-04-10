package kr.hhplus.be.server.application.dataplatform

import kr.hhplus.be.server.domain.order.Order

sealed class DataPlatformCriteria {
    data class OrderMessage(
        val orderId: Long,
        val customerId: Long,
        val totalPrice: Int,
        val createdAt: String,
        val items: List<OrderItemMessage>
    ) {
        companion object {
            fun from(order: Order): OrderMessage {
                return OrderMessage(
                    orderId = order.requireSavedId(),
                    customerId = order.customer.requireSavedId(),
                    totalPrice = order.totalPrice,
                    createdAt = order.createdAt.toString(),
                    items = order.orderItems.map {
                        OrderItemMessage(
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

    data class OrderItemMessage(
        val productName: String,
        val optionName: String,
        val quantity: Int,
        val subtotalPrice: Int
    )
}
