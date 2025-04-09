package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.order.OrderItem
import java.time.LocalDateTime

class ProductOption(
    val product: Product,
    var optionName: String,
    var extraPrice: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var stock: Stock? = null
    var orderItems: MutableList<OrderItem> = mutableListOf()
}
