package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.Order
import java.time.LocalDateTime

data class PaymentCompletedEvent(
    val orderId: Long,
    val customerId: Long,
    val totalPrice: Int,
    val createdAt: LocalDateTime,
    val items: List<Item>
) {
    constructor() : this(0L, 0L, 0, LocalDateTime.now(), emptyList())

    data class Item(
        val productId: Long = 0L,
        val productName: String = "",
        val optionName: String = "",
        val quantity: Int = 0,
        val subtotalPrice: Int = 0
    )

    companion object {
        fun from(order: Order): PaymentCompletedEvent {
            return PaymentCompletedEvent(
                orderId = order.id,
                customerId = order.customer.id,
                totalPrice = order.totalPrice,
                createdAt = order.createdAt,
                items = order.orderItems.map {
                    Item(
                        productId = it.productOption.product.id,
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
