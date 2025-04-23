package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CouponInvalidPeriodException
import java.time.LocalDate

@Entity
@Table(name = "coupon")
class Coupon private constructor(
    @Column(name = "name", nullable = false, length = 50)
    val name: String,

    @Column(name = "total_quantity", nullable = false)
    val totalQuantity: Int,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDate,

    @Column(name = "expired_at", nullable = false)
    val expiredAt: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    val discountType: DiscountType,

    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Int
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "current_quantity", nullable = false)
    private var currentQuantity: Int = totalQuantity

    @Transient
    private var discountPolicy: DiscountPolicy  =
        when (discountType) {
            DiscountType.FIXED -> FixedDiscountPolicy(discountAmount)
            DiscountType.RATE -> RateDiscountPolicy(discountAmount)
        }

    companion object {
        fun createFixedDiscount(
            name: String,
            amount: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountType = DiscountType.FIXED,
                discountAmount = amount
            )
        }

        fun createRateDiscount(
            name: String,
            rate: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountType = DiscountType.RATE,
                discountAmount = rate
            )
        }
    }

    @PostLoad
    fun initDiscountPolicy() {
        discountPolicy = when (discountType) {
            DiscountType.FIXED -> FixedDiscountPolicy(discountAmount)
            DiscountType.RATE -> RateDiscountPolicy(discountAmount)
        }
    }

    fun decreaseQuantity() {
        if (currentQuantity <= 0) {
            throw CouponInsufficientException()
        }

        currentQuantity -= 1
    }

    fun calculateDiscount(totalPrice: Int): Int {
        validatePeriod()
        return discountPolicy.calculateDiscount(totalPrice)
    }

    fun validatePeriod(now: LocalDate = LocalDate.now()) {
        if (now.isBefore(startedAt) || now.isAfter(expiredAt)) {
            throw CouponInvalidPeriodException()
        }
    }
}
