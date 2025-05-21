package kr.hhplus.be.server.domain.coupon.event

data class CouponUsedEvent(
    val orderId: Long,
    val discountAmount: Int,
    val discountedPrice: Int
)
