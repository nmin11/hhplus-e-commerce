package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import java.time.LocalDate
import java.time.LocalDateTime

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
    private val discountPolicy: DiscountPolicy  =
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

    fun decreaseQuantity() {
        if (currentQuantity <= 0) {
            throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다.")
        }

        currentQuantity -= 1
        updatedAt = LocalDateTime.now()
    }

    fun calculateDiscount(totalPrice: Int): Int {
        validatePeriod()
        return discountPolicy.calculateDiscount(totalPrice)
    }

    fun validatePeriod(now: LocalDate = LocalDate.now()) {
        if (now.isBefore(startedAt) || now.isAfter(expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }
    }
}
