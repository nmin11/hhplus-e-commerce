package kr.hhplus.be.server.domain.payment

import java.time.LocalDateTime

class Payment(
    val orderId: Long,
    val customerId: Long,
    val couponId: Long?,
    val originalPrice: Int,
    val discountAmount: Int,
    val discountedPrice: Int
) {
    var id: Long? = null
    val paidAt: LocalDateTime = LocalDateTime.now()
}
