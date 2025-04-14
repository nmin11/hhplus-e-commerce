package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

class CustomerCoupon private constructor(
    val customerId: Long,
    val couponId: Long
) {
    val id: Long = 0L
    var status = CustomerCouponStatus.AVAILABLE
    val issuedAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun issue(customerId: Long, couponId: Long): CustomerCoupon {
            return CustomerCoupon(customerId, couponId)
        }
    }

    fun expireIfAvailable() {
        if (status == CustomerCouponStatus.AVAILABLE) {
            status = CustomerCouponStatus.EXPIRED
            updatedAt = LocalDateTime.now()
        }
    }

    fun validateUsable(): CustomerCoupon {
        if (status == CustomerCouponStatus.USED) {
            throw IllegalStateException("이미 사용된 쿠폰입니다.")
        }
        if (status == CustomerCouponStatus.EXPIRED) {
            throw IllegalStateException("사용 기간이 만료된 쿠폰입니다.")
        }
        return this
    }
}
