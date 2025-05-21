package kr.hhplus.be.server.domain.coupon.event

data class CouponRollbackRequestedEvent(
    val customerId: Long,
    val couponId: Long
)
