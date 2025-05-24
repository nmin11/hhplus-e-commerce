package kr.hhplus.be.server.domain.product.event

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.product.Product

data class StatisticRecordRequestedEvent(
    val orderId: Long,
    val items: List<SoldItem>
) {
    data class SoldItem(
        val product: Product,
        val quantity: Int
    )

    companion object {
        fun from(order: Order): StatisticRecordRequestedEvent {
            return StatisticRecordRequestedEvent(
                orderId = order.id,
                items = order.orderItems.map {
                    SoldItem(
                        product = it.productOption.product,
                        quantity = it.quantity
                    )
                }
            )
        }
    }
}
