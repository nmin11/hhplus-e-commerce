package kr.hhplus.be.server.domain.product.event

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.product.ProductInfo

data class StockDecreaseRequestedEvent(
    val orderId: Long,
    val items: List<ProductInfo.StockItem>
) {
    companion object {
        fun from(order: Order): StockDecreaseRequestedEvent {
            return StockDecreaseRequestedEvent(
                orderId = order.id,
                items = order.orderItems.map {
                    ProductInfo.StockItem(
                        productOptionId = it.productOption.id,
                        quantity = it.quantity
                    )
                }
            )
        }
    }
}
