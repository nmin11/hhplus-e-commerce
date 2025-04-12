package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.application.coupon.CustomerCouponResult

sealed class CouponResponse {
    data class Issue(
        val couponId: Long,
        val customerId: Long,
        val status: String,
        val issuedAt: String
    ) {
        companion object {
            fun from(result: CouponResult.Issue): Issue {
                return Issue(
                    couponId = result.couponId,
                    customerId = result.customerId,
                    status = result.status,
                    issuedAt = result.issuedAt
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
            fun from(result: CustomerCouponResult): Owned {
                return Owned(
                    name = result.name,
                    discountType = result.discountType,
                    discountAmount = result.discountAmount,
                    status = result.status.name,
                    issuedAt = result.issuedAt,
                    expiredAt = result.expiredAt
                )
            }
        }
    }
}
