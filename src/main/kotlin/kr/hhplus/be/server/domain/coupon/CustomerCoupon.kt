package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

class CustomerCoupon private constructor(
    val customer: Customer,
    val coupon: Coupon
) {
    var id: Long? = null
    var status = CustomerCouponStatus.AVAILABLE
    val issuedAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
            return CustomerCoupon(
                customer = customer,
                coupon = coupon
            )
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

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("CustomerCoupon 객체가 저장되지 않았습니다.")
}
