package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import java.time.LocalDateTime

class Payment private constructor(
    val order: Order,
    val customer: Customer,
    val coupon: Coupon?,
    val originalPrice: Int,
    val discountAmount: Int,
    val discountedPrice: Int
) {
    val id: Long = 0L
    val paidAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(
            order: Order,
            customer: Customer,
            originalPrice: Int,
            discountAmount: Int,
            coupon: Coupon? = null
        ): Payment {
            require(originalPrice >= 0) { "기존 금액은 0 이상이어야 합니다." }
            require(discountAmount >= 0) { "할인 금액은 0 이상이어야 합니다." }
            require(discountAmount <= originalPrice) { "할인 금액은 총 주문 금액보다 낮아야 합니다." }

            val discountedPrice = originalPrice - discountAmount

            return Payment(
                order = order,
                customer = customer,
                coupon = coupon,
                originalPrice = originalPrice,
                discountAmount = discountAmount,
                discountedPrice = discountedPrice
            )
        }
    }
}
