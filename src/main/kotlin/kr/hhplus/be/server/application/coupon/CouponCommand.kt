package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.interfaces.coupon.CouponRequest

sealed class CouponCommand {
    data class Issue(
        val couponId: Long,
        val customerId: Long
    ) {
        companion object {
            fun from(request: CouponRequest.Issue): Issue {
                return Issue(
                    couponId = request.couponId,
                    customerId = request.customerId
                )
            }
        }
    }
}
