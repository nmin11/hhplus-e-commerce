package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Stock(
    val productOption: ProductOption,
    var quantity: Int
) {
    var id: Long? = null
    var updatedAt: LocalDateTime = LocalDateTime.now()

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Stock 객체가 저장되지 않았습니다.")
}
