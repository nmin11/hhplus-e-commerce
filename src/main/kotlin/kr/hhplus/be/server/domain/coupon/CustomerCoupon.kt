package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

class CustomerCoupon(
    val customerId: Long,
    val couponId: Long,
) {
    var id: Long? = null
    val status = CustomerCouponStatus.AVAILABLE
    val issuedAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}
