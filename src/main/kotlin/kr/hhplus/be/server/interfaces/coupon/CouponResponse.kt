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
                    couponId = customerCoupon.coupon.requireSavedId(),
                    customerId = customerCoupon.customer.requireSavedId(),
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
    ) {
        companion object {
            fun from(customerCoupon: CustomerCoupon): Owned {
                val coupon = customerCoupon.coupon
                return Owned(
                    name = coupon.name,
                    discountType = coupon.discountType.name,
                    discountAmount = coupon.discountAmount,
                    status = customerCoupon.status.name,
                    issuedAt = customerCoupon.issuedAt.toString(),
                    expiredAt = coupon.expiredAt.toString()
                )
            }
        }
    }
}
