package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponStatus

sealed class CouponResult {
    data class Issue(
        val couponId: Long,
        val customerId: Long,
        val status: String = CustomerCouponStatus.AVAILABLE.name,
    ) {
        companion object {
            fun from(customerCoupon: CustomerCoupon): Issue {
                return Issue(
                    couponId = customerCoupon.coupon.id,
                    customerId = customerCoupon.customer.id,
                    status = customerCoupon.status.name,
                )
            }
        }
    }
}
