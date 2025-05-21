package kr.hhplus.be.server.domain.product.event

import kr.hhplus.be.server.domain.product.ProductInfo

data class StockDecreasedEvent(
    val orderId: Long,
    val items: List<ProductInfo.StockItem>
) {
    companion object {
        fun from(event: StockDecreaseRequestedEvent): StockDecreasedEvent {
            return StockDecreasedEvent(
                orderId = event.orderId,
                items = event.items.map {
                    ProductInfo.StockItem(it.productOptionId, it.quantity)
                }
            )
        }
    }
}
