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
    var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
            return CustomerCoupon(
                customer = customer,
                coupon = coupon
            )
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("CustomerCoupon 객체가 저장되지 않았습니다.")
}
