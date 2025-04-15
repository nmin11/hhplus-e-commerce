package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponStatus

data class CustomerCouponResult(
    val name: String,
    val discountType: String,
    val discountAmount: Int,
    val status: CustomerCouponStatus,
    val issuedAt: String,
    val expiredAt: String
) {
    companion object {
        fun from(customerCoupon: CustomerCoupon): CustomerCouponResult {
            val coupon = customerCoupon.coupon
            return CustomerCouponResult(
                name = coupon.name,
                discountType = coupon.discountType.name,
                discountAmount = coupon.discountAmount,
                status = customerCoupon.status,
                issuedAt = customerCoupon.issuedAt.toString(),
                expiredAt = coupon.expiredAt.toString()
            )
        }
    }
}
