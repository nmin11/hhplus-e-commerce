package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.order.OrderItem
import java.time.LocalDateTime

class ProductOption private constructor(
    val product: Product,
    val optionName: String,
    val extraPrice: Int
) {
    val id: Long = 0L
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var stock: Stock? = null
    val orderItems: MutableList<OrderItem> = mutableListOf()

    companion object {
        fun create(product: Product, optionName: String, extraPrice: Int): ProductOption {
            require(optionName.isNotBlank()) { "옵션 이름은 공백일 수 없습니다." }
            require(extraPrice >= 0) { "추가 가격은 0 이상이어야 합니다." }

            return ProductOption(product, optionName, extraPrice)
        }
    }
}
