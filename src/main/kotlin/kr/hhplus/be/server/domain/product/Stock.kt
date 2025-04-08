package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Stock(
    val productOptionId: Long,
    val quantity: Int
) {
    var id: Long? = null
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
