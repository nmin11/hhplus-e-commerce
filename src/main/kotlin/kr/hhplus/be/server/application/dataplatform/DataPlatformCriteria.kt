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
                    orderId = order.id ?: throw IllegalStateException("주문 ID가 없습니다."),
                    customerId = order.customer.id ?: throw IllegalStateException("고객 ID가 없습니다."),
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
