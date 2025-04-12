package kr.hhplus.be.server.interfaces.coupon

sealed class CouponRequest {
    data class Issue(
        val couponId: Long,
        val customerId: Long
    )
}
