package kr.hhplus.be.server.domain.product.event

import kr.hhplus.be.server.domain.product.ProductInfo

data class StockRollbackRequestedEvent(
    val orderId: Long,
    val items: List<ProductInfo.StockItem>
)
