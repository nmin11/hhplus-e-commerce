package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import java.time.LocalDateTime

class Payment(
    val order: Order,
    val customer: Customer,
    var coupon: Coupon? = null,
    var originalPrice: Int,
    var discountAmount: Int,
    var discountedPrice: Int
) {
    var id: Long? = null
    val paidAt: LocalDateTime = LocalDateTime.now()
}
