package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

class CustomerCoupon(
    val customer: Customer,
    val coupon: Coupon
) {
    var id: Long? = null
    var status = CustomerCouponStatus.AVAILABLE
    val issuedAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
