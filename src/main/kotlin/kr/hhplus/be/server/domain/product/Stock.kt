package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Stock(
    val productOption: ProductOption,
    var quantity: Int
) {
    var id: Long? = null
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
