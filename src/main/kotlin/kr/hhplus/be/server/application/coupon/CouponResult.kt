package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CustomerCoupon

sealed class CouponResult {
    data class Issue(
        val couponId: Long,
        val customerId: Long,
        val status: String,
        val issuedAt: String
    ) {
        companion object {
            fun from(customerCoupon: CustomerCoupon): Issue {
                return Issue(
                    couponId = customerCoupon.coupon.requireSavedId(),
                    customerId = customerCoupon.customer.requireSavedId(),
                    status = customerCoupon.status.name,
                    issuedAt = customerCoupon.issuedAt.toString()
                )
            }
        }
    }
}
