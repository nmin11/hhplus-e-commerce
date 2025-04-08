package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class ProductOption(
    val productId: Long,
    val optionName: String,
    val extraPrice: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
