package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.domain.coupon.CustomerCoupon

sealed class CouponResponse {
    data class Issue(
        val couponId: Long,
        val customerId: Long,
        val status: String,
        val issuedAt: String
    ) {
        companion object {
            fun from(customerCoupon: CustomerCoupon): Issue {
                return Issue(
                    couponId = customerCoupon.coupon.id ?: throw IllegalStateException("쿠폰 ID가 없습니다."),
                    customerId = customerCoupon.customer.id ?: throw IllegalStateException("고객 ID가 없습니다."),
                    status = customerCoupon.status.name,
                    issuedAt = customerCoupon.issuedAt.toString()
                )
            }
        }
    }

    data class Owned(
        val name: String,
        val discountType: String,
        val discountAmount: Int,
        val status: String,
        val issuedAt: String,
        val expiredAt: String
    )
}
