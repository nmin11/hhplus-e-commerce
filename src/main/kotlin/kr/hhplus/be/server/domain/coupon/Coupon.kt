package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

class Coupon(
    val name: String,
    val discountType: DiscountType,
    val discountAmount: Int,
    val currentQuantity: Int,
    val totalQuantity: Int,
    val startedAt: LocalDateTime,
    val expiredAt: LocalDateTime
) {
    val id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
