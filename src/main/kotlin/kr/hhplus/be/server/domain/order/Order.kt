package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

class Order(
    val customerId: Long,
    val totalPrice: Int,
) {
    val id: Long? = null
    val status = OrderStatus.CREATED
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
