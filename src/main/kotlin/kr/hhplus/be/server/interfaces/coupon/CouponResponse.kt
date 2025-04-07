package kr.hhplus.be.server.interfaces.coupon

sealed class CouponResponse {
    data class Issue(
        val couponId: Long,
        val customerId: Long,
        val status: String,
        val issuedAt: String
    )

    data class Owned(
        val name: String,
        val discountType: String,
        val discountAmount: Int,
        val status: String,
        val issuedAt: String,
        val expiredAt: String
    )
}
