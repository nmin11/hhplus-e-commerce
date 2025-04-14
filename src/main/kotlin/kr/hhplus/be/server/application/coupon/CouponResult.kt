package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponStatus

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
                    couponId = customerCoupon.couponId,
                    customerId = customerCoupon.customerId,
                    status = customerCoupon.status.name,
                    issuedAt = customerCoupon.issuedAt.toString()
                )
            }
        }
    }

    data class OwnedCoupon(
        val name: String,
        val discountType: String,
        val discountAmount: Int,
        val status: CustomerCouponStatus,
        val issuedAt: String,
        val expiredAt: String
    ) {
        companion object {
            fun from(customerCoupon: CustomerCoupon, coupon: Coupon): OwnedCoupon {
                return OwnedCoupon(
                    name = coupon.name,
                    discountType = coupon.discountPolicy.getType(),
                    discountAmount = coupon.discountPolicy.getAmount(),
                    status = customerCoupon.status,
                    issuedAt = customerCoupon.issuedAt.toString(),
                    expiredAt = coupon.expiredAt.toString()
                )
            }
        }
    }
}
