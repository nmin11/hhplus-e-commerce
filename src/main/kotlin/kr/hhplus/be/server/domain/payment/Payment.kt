package kr.hhplus.be.server.domain.payment

import jakarta.persistence.*
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.order.Order
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class Payment private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    val coupon: Coupon? = null,

    @Column(name = "original_price", nullable = false)
    val originalPrice: Int,

    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Int,

    @Column(name = "discounted_price", nullable = false)
    val discountedPrice: Int
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "paid_at", nullable = false)
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
