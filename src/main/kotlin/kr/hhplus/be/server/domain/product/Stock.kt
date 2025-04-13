package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Stock private constructor(
    val productOption: ProductOption,
    var quantity: Int
) {
    var id: Long? = null
    var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(productOption: ProductOption, quantity: Int): Stock {
            require(quantity >= 0) { "재고 수량은 0 이상이어야 합니다." }

            return Stock(productOption, quantity)
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Stock 객체가 저장되지 않았습니다.")
}
