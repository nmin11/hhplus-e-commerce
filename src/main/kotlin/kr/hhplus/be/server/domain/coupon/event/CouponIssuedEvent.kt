package kr.hhplus.be.server.domain.coupon.event

data class CouponIssuedEvent(
    val couponId: Long = 0L,
    val customerId: Long = 0L
)
