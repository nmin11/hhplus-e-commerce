package kr.hhplus.be.server.domain.coupon.event

data class CouponUseRequestedEvent(
    val orderId: Long,
    val customerId: Long,
    val couponId: Long,
    val totalPrice: Int
)
